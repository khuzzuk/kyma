package net.kyma.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SupportedFields;
import net.kyma.dm.SoundFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Bits;
import pl.khuzzuk.messaging.Bus;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.IOException;
import java.util.*;

import static net.kyma.data.QueryUtils.termForPath;

@Singleton
@Log4j2
public class DataIndexer {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private IndexWriter writer;
    @Inject
    private DocConverter docConverter;
    private Set<String> indexedPaths;

    @PostConstruct
    public void init() {
        bus.setReaction(messages.getProperty("close"), this::close);
        bus.setReaction(messages.getProperty("data.index.list"), this::index);
        bus.setReaction(messages.getProperty("data.store.list"), this::index);
        bus.setReaction(messages.getProperty("data.index.item"), this::indexSingleEntity);
        bus.setReaction(messages.getProperty("data.store.item"), this::indexSingleEntity);
        bus.setResponse(messages.getProperty("data.index.getAll"), this::getAll);
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("data.remove.item"), c -> c.forEach(this::remove));
        bus.<Set<String>>setReaction(messages.getProperty("data.index.get.directories"),
                paths -> indexedPaths = paths);
        refreshIndexedPaths();
    }

    private synchronized void index(@NonNull Collection<SoundFile> files) {
        if (files.isEmpty()) return;

        String path = files.iterator().next().getIndexedPath();
        indexedPaths.stream().filter(path::startsWith).findAny()
                .ifPresent(indexed -> files.forEach(f -> f.setIndexedPath(indexed)));

        files.forEach(this::indexSingleEntity);
        bus.sendCommunicate(messages.getProperty("data.index.getAll"), messages.getProperty("data.convert.from.doc.gui"));
        refreshIndexedPaths();
    }

    private void indexSingleEntity(@NonNull SoundFile soundFile) {
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            if (isNew(soundFile, searcher)) {
                writer.addDocument(docConverter.docFrom(soundFile));
            } else {
                normalizeIndexingPath(soundFile, searcher);
                writer.updateDocument(termForPath(soundFile.getPath()), docConverter.docFrom(soundFile));
            }
        } catch (IOException e) {
            log.error("Indexing occurred error");
            log.error(e);
        }
    }

    private void normalizeIndexingPath(SoundFile soundFile, IndexSearcher searcher) {
        String previousPath = soundFile.getIndexedPath();
        try {
            ScoreDoc[] scoreDocs = searcher.search(new TermQuery(termForPath(soundFile.getPath())), 1).scoreDocs;
            if (scoreDocs.length == 1) {
                previousPath = searcher.doc(scoreDocs[0].doc, Collections.singleton(SupportedFields.INDEXED_PATH.getName()))
                        .get(SupportedFields.INDEXED_PATH.getName());
            }
        } catch (IOException e) {
            log.error("Cannot read data");
            log.error(e);
        }
        soundFile.setIndexedPath(previousPath);
    }

    private synchronized Collection<Document> getAll() {
        Collection<Document> documents = new ArrayList<>();
        try {
            DirectoryReader reader = DirectoryReader.open(writer);
            int maxDoc = reader.maxDoc();
            Bits liveDocs = MultiFields.getLiveDocs(reader);
            for (int i = 0; i < maxDoc; i++) {
                if (liveDocs == null || liveDocs.get(i)) {
                    documents.add(reader.document(i));
                }
            }
            reader.close();
        } catch (IOException e) {
            log.error("Cannot read data");
            log.error(e);
        }

        return documents;
    }

    private boolean isNew(SoundFile soundFile, IndexSearcher searcher) {
        try {
            return searcher.search(new TermQuery(termForPath(soundFile.getPath())), 1)
                    .scoreDocs.length == 0;
        } catch (IOException e) {
            log.error("Cannot read data");
            log.error(e);
        }
        return false;
    }

    private void remove(SoundFile soundFile) {
        try {
            writer.deleteDocuments(termForPath(soundFile.getPath()));
        } catch (IOException e) {
            log.error("Cannot delete document, problem with accessing the index files");
            log.error(e);
        }
    }

    private void refreshIndexedPaths() {
        bus.send(messages.getProperty("data.index.get.distinct"),
                messages.getProperty("data.index.get.directories"),
                SupportedFields.INDEXED_PATH);
    }

    private void close() {
        try {
            writer.close();
        } catch (IOException e) {
            log.error("error on closing database");
            log.error(e);
        }
        //TODO refactoring, change place where bus is closed
        bus.closeBus();
    }
}
