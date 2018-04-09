package net.kyma.data;

import static java.lang.Integer.MAX_VALUE;
import static net.kyma.EventType.DATA_CONVERT_FROM_DOC;
import static net.kyma.EventType.DATA_QUERY;
import static net.kyma.EventType.RET_INDEX_WRITER;
import static net.kyma.dm.SupportedField.INDEXED_PATH;
import static net.kyma.dm.SupportedField.PATH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SupportedField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class DataReader implements Loadable
{
    private final Bus<EventType> bus;
    private IndexWriter writer;

    @Override
    public void load() {
        bus.subscribingFor(RET_INDEX_WRITER).accept(this::setWriter).subscribe();
    }

    private void setWriter(IndexWriter writer)
    {
        this.writer = writer;
        bus.subscribingFor(EventType.DATA_GET_PATHS).withResponse(this::getFilePaths).subscribe();
        bus.subscribingFor(EventType.DATA_INDEX_GET_DISTINCT).mapResponse(this::getDistinctValues).subscribe();
        bus.subscribingFor(DATA_QUERY).accept(this::search).subscribe();
    }

    private Map<String, Set<String>> getFilePaths() {
        Map<String, Set<String>> paths = new TreeMap<>();
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs search = searcher.search(new WildcardQuery(new Term(PATH.getName(), "*")), MAX_VALUE);
            Set<String> queryField = Set.of(PATH.getName(), INDEXED_PATH.getName());

            for (ScoreDoc doc : search.scoreDocs) {
                Document document = searcher.doc(doc.doc, queryField);
                String path = document.get(PATH.getName());
                String indexedPath = document.get(INDEXED_PATH.getName());

                paths.computeIfAbsent(indexedPath, s -> new TreeSet<>())
                      .add(path.replaceFirst(indexedPath, ""));
            }
        } catch (IOException e) {
            log.error("Error during paths refreshing from index", e);
        }
        return paths;
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

    private void search(QueryParameters parameters) {
        List<Document> documents = new ArrayList<>();
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            ScoreDoc[] docs = parameters.performQuery(indexSearcher);
            for (ScoreDoc doc : docs) {
                documents.add(reader.document(doc.doc));
            }
            bus.message(DATA_CONVERT_FROM_DOC).withResponse(parameters.getReturnTopic()).withContent(documents).send();
        }
        catch (IOException e)
        {
            log.error("cannot query index", e);
        }
    }
}
