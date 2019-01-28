package net.kyma.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.BusRequestException;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.DataQuery;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Bits;
import pl.khuzzuk.messaging.Bus;

import java.io.IOException;
import java.util.*;

import static net.kyma.EventType.*;
import static net.kyma.data.QueryUtils.termForPath;

@Log4j2
@RequiredArgsConstructor
public class DataIndexer implements Loadable {
   private final Bus<EventType> bus;
   private IndexWriter writer;
   private Set<String> indexedPaths;
   private static final String READ_DATA_ERROR_MESSAGE = "Cannot read data";

   @Override
   public void load() {
      bus.subscribingFor(RET_INDEX_WRITER).accept(this::init).subscribe();
   }

   public void init(IndexWriter writer) {
      this.writer = writer;
      bus.subscribingFor(CLOSE).then(this::close).subscribe();
      bus.subscribingFor(DATA_INDEX_CLEAN).then(this::clearIndex).subscribe();
      bus.subscribingFor(DATA_INDEX_LIST).accept(this::index).subscribe();
      bus.subscribingFor(DATA_STORE_LIST).accept(this::index).subscribe();
      bus.subscribingFor(DATA_INDEX_ITEM).accept(this::indexSingleEntity).subscribe();
      bus.subscribingFor(DATA_STORE_ITEM).accept(this::indexSingleEntity).subscribe();
      bus.subscribingFor(DATA_INDEX_GET_ALL).then(this::getAll).subscribe();
      bus.subscribingFor(DATA_REMOVE_ITEM).<Collection<SoundFile>>accept(this::remove).subscribe();
      bus.subscribingFor(DATA_REMOVE_PATH).accept(this::removePath).subscribe();
      bus.subscribingFor(DATA_INDEX_GET_DIRECTORIES).<Set<String>>accept(paths -> indexedPaths = paths).subscribe();
      refreshIndexedPaths();
   }

   private synchronized void index(@NonNull Collection<SoundFile> files) {
      if (!files.isEmpty()) {
         String path = files.iterator().next().getIndexedPath();
         indexedPaths.stream().filter(path::startsWith).findAny()
               .ifPresent(indexed -> files.forEach(f -> f.setIndexedPath(indexed)));
      }

      indexMultipleEntities(files);
      commit();
      refreshIndexedPaths();
   }

   private void indexSingleEntity(@NonNull SoundFile soundFile) {
      try (DirectoryReader reader = DirectoryReader.open(writer)) {
         IndexSearcher searcher = new IndexSearcher(reader);
         addDocument(soundFile, searcher);
      } catch (IOException e) {
         reportIndexingError(e);
      }
   }

   private void indexMultipleEntities(Collection<SoundFile> files) {
      try (DirectoryReader reader = DirectoryReader.open(writer)) {
         IndexSearcher searcher = new IndexSearcher(reader);
         for (SoundFile soundFile : files) addDocument(soundFile, searcher);
      } catch (IOException e) {
         reportIndexingError(e);
      }
   }

   private void addDocument(SoundFile soundFile, IndexSearcher searcher) throws IOException {
      if (isNew(soundFile, searcher)) {
         writer.addDocument(DocConverter.docFrom(soundFile));
      } else {
         normalizeIndexingPath(soundFile, searcher);
         writer.updateDocument(termForPath(soundFile.getPath()), DocConverter.docFrom(soundFile));
      }
   }

   private void reportIndexingError(IOException e) {
      bus.message(SHOW_ALERT).withContent(String.format("Error during indexing: %s", e.getMessage()));
      bus.message(SHOW_ALERT).withContent(String.format("Indexing error report: %s", Arrays.toString(e.getStackTrace())));
   }

   private void normalizeIndexingPath(SoundFile soundFile, IndexSearcher searcher)
   {
      String previousPath = soundFile.getIndexedPath();
      try {
         ScoreDoc[] scoreDocs = searcher.search(new TermQuery(termForPath(soundFile.getPath())), 1).scoreDocs;
         if (scoreDocs.length == 1)
         {
            previousPath = searcher.doc(scoreDocs[0].doc, Collections.singleton(SupportedField.INDEXED_PATH.getName()))
                  .get(SupportedField.INDEXED_PATH.getName());
         }
      } catch (IOException e) {
         log.error(READ_DATA_ERROR_MESSAGE, e);
      }
      soundFile.setIndexedPath(previousPath);
   }

   private synchronized void getAll() {
      Collection<Document> documents = new ArrayList<>();
      try (DirectoryReader reader = DirectoryReader.open(writer)) {
         int maxDoc = reader.maxDoc();
         Bits liveDocs = MultiFields.getLiveDocs(reader);
         for (int i = 0; i < maxDoc; i++)
         {
            if (liveDocs == null || liveDocs.get(i))
            {
               documents.add(reader.document(i));
            }
         }
      } catch (IOException e) {
         log.error(READ_DATA_ERROR_MESSAGE, e);
      }

      bus.message(DATA_CONVERT_FROM_DOC).withResponse(DATA_REFRESH).withContent(documents).send();
   }

   private boolean isNew(SoundFile soundFile, IndexSearcher searcher) {
      try {
         return searcher.search(new TermQuery(termForPath(soundFile.getPath())), 1)
               .scoreDocs.length == 0;
      } catch (IOException e) {
         log.error(READ_DATA_ERROR_MESSAGE, e);
      }
      return false;
   }

   private void remove(Collection<SoundFile> soundFiles) {
      soundFiles.forEach(this::remove);
      commit();
   }

   private void remove(SoundFile soundFile) {
      try {
         writer.deleteDocuments(termForPath(soundFile.getPath()));
      } catch (IOException e) {
         log.error("Cannot delete document, problem with accessing the index files", e);
      }
   }

   private void removePath(DataQuery query) {
      try {
         long deleteDocuments = writer.deleteDocuments(QueryUtils.from(query));
         commit();
         log.debug("Deleted documents: {}", deleteDocuments);
      } catch (IOException e) {
         bus.message(SHOW_ALERT).withContent(String.format("Cannot remove index entries for path: %s", e.getMessage())).send();
         bus.message(SHOW_ALERT).withContent(String.format("Remove report: %s", Arrays.toString(e.getStackTrace()))).send();
         throw new BusRequestException(e);
      }
   }

   private void refreshIndexedPaths() {
      bus.message(DATA_INDEX_GET_DISTINCT)
            .withResponse(DATA_INDEX_GET_DIRECTORIES)
            .withContent(SupportedField.INDEXED_PATH)
            .send();
      bus.message(DATA_GET_PATHS).send();
   }

   private void commit() {
      try {
         writer.commit();
      } catch (IOException e) {
         bus.message(SHOW_ALERT).withContent(String.format("Indexing acces Problem: %s", e.getMessage())).send();
      }
   }

   private void clearIndex() {
      try {
         writer.deleteDocuments(new MatchAllDocsQuery());
         refreshIndexedPaths();
      } catch (IOException e) {
         log.error("Cannot clear index", e);
         bus.message(SHOW_ALERT).withContent("Indexing access problem").send();
      }
   }

   private void close() {
      try {
         writer.close();
      } catch (IOException e) {
         log.error("error on closing database", e);
      }
      if (writer.isOpen()) {
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            log.error("Error during close operation", e);
            Thread.currentThread().interrupt();
         }
         close();
      }
      //TODO refactoring, change place where bus is closed
      bus.closeBus();
   }
}
