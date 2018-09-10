package net.kyma.data;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.DataQuery;
import net.kyma.dm.IndexingRoot;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.initialization.Dependable;
import net.kyma.initialization.Dependency;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import pl.khuzzuk.messaging.Bus;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;
import static net.kyma.EventType.*;
import static net.kyma.dm.SupportedField.INDEXED_PATH;
import static net.kyma.dm.SupportedField.PATH;

@Log4j2
@RequiredArgsConstructor
public class DataReader extends Dependable implements Loadable {
    private final Bus<EventType> bus;
    @Setter
    @Dependency
    private IndexWriter writer;
    @Setter
    @Dependency
    private SoundFileConverter soundFileConverter;

    @Override
    public void load() {
        bus.subscribingFor(RET_INDEX_WRITER).accept(this::setWriter).subscribe();
        bus.subscribingFor(RET_SOUND_FILE_CONVERTER).accept(this::setSoundFileConverter).subscribe();
    }

    @Override
    public void afterDependenciesSet()
    {
        bus.subscribingFor(EventType.DATA_GET_PATHS).then(this::getFilePaths).subscribe();
        bus.subscribingFor(EventType.DATA_INDEX_GET_DISTINCT).mapResponse(this::getDistinctValues).subscribe();
        bus.subscribingFor(DATA_QUERY).mapResponse(this::search).subscribe();
    }

    private void getFilePaths() {
        Map<IndexingRoot, Set<String>> paths = new TreeMap<>();
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs search = searcher.search(new WildcardQuery(new Term(PATH.getName(), "*")), MAX_VALUE);
            Set<String> queryField = Set.of(PATH.getName(), INDEXED_PATH.getName());

            for (ScoreDoc doc : search.scoreDocs) {
                Document document = searcher.doc(doc.doc, queryField);
                String path = document.get(PATH.getName());
                IndexingRoot indexedPath = IndexingRoot.forPathOnDisk(document.get(INDEXED_PATH.getName()));

                paths.computeIfAbsent(indexedPath, s -> new TreeSet<>())
                        .add(path.replaceFirst(indexedPath.toPathRepresentation(), ""));
            }
        } catch (IOException e) {
            log.error("Error during paths refreshing from index", e);
        }
        bus.message(DATA_REFRESH_PATHS).withContent(paths).send();
    }

    private Set<String> getDistinctValues(SupportedField field) {
        Set<String> values = new TreeSet<>();
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs search = searcher.search(new WildcardQuery(new Term(field.getName(), "*")), MAX_VALUE);
            Set<String> queryField = Collections.singleton(field.getName());
            for (ScoreDoc doc : search.scoreDocs) {
                values.add(searcher.doc(doc.doc, queryField).get(field.getName()));
            }
        } catch (IOException e) {
            log.error("cannot query index", e);
        }
        return values;
    }

    private Collection<SoundFile> search(DataQuery query) {
        List<Document> documents = new ArrayList<>();
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            ScoreDoc[] docs = indexSearcher.search(QueryUtils.from(query), Integer.MAX_VALUE).scoreDocs;
            for (ScoreDoc doc : docs) {
                documents.add(reader.document(doc.doc));
            }
        } catch (IOException e) {
            log.error("cannot query index", e);
        }
        return documents.stream().map(soundFileConverter::from).collect(Collectors.toList());
    }
}
