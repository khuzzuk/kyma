package net.kyma.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.MetadataField;
import net.kyma.dm.SoundFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Bits;
import pl.khuzzuk.messaging.Bus;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

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

    public void init() {
        bus.setReaction(messages.getProperty("close"), this::close);
        bus.setReaction(messages.getProperty("data.index.list"), this::index);
        bus.setResponse(messages.getProperty("data.index.getAll"), this::getAll);
    }

    private synchronized void index(Collection<SoundFile> files) {
        persist(files.stream().map(docConverter::docFrom).collect(Collectors.toList()));
        bus.sendCommunicate(messages.getProperty("data.index.getAll"), messages.getProperty("data.convert.from.doc.gui"));
    }

    private void persist(Document document) {
        try {
            writer.addDocument(document);
        } catch (IOException e) {
            log.error("error occured when file was indexed: " + document.get(MetadataField.FILE_NAME.getName()));
            log.error(e);
        }
    }

    private void persist(Collection<Document> documents) {
        try {
            writer.addDocuments(documents);
        } catch (IOException e) {
            log.error("Indexing occurred error");
            log.error(e);
        }
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

    private void close() {
        try {
            writer.close();
        } catch (IOException e) {
            log.error("error on closing database");
            log.error(e);
        }
        bus.closeBus();
    }
}
