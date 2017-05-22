package net.kyma.data;

import com.google.inject.Inject;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.MetadataField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;

@Singleton
@Log4j2
public class DataReader {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private IndexWriter writer;

    public void init() {
        bus.setResponse(messages.getProperty("data.index.get.distinct"), this::getDistinctValues);
    }

    private Set<String> getDistinctValues(MetadataField field) {
        Set<String> values = new TreeSet<>();
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs search = searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);
            for (ScoreDoc doc : search.scoreDocs) {
                values.add(searcher.doc(doc.doc, Collections.singleton(field.getName())).get(field.getName()));
            }
        } catch (IOException e) {
            log.error("cannot query index");
            log.error(e);
        }
        return values;
    }
}
