package net.kyma.data;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SupportedField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@AllArgsConstructor
public class DataReader implements Loadable
{
    private Bus<EventType> bus;
    private IndexWriter writer;

    @Override
    public void load() {
        bus.setResponse(EventType.DATA_INDEX_GET_DISTINCT, this::getDistinctValues);
    }

    private Set<String> getDistinctValues(SupportedField field) {
        Set<String> values = new TreeSet<>();
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs search = searcher.search(new WildcardQuery(new Term(field.getName(), "*")), Integer.MAX_VALUE);
            Set<String> queryField = Collections.singleton(field.getName());
            for (ScoreDoc doc : search.scoreDocs) {
                values.add(searcher.doc(doc.doc, queryField).get(field.getName()));
            }
        } catch (IOException e) {
            log.error("cannot query index");
            log.error(e);
        }
        return values;
    }
}
