package net.kyma.data;

import static net.kyma.EventType.CLOSE;
import static net.kyma.EventType.DATA_CONVERT_FROM_DOC;
import static net.kyma.EventType.DATA_GET_PATHS;
import static net.kyma.EventType.DATA_INDEX_GET_ALL;
import static net.kyma.EventType.DATA_INDEX_GET_DIRECTORIES;
import static net.kyma.EventType.DATA_INDEX_GET_DISTINCT;
import static net.kyma.EventType.DATA_INDEX_ITEM;
import static net.kyma.EventType.DATA_INDEX_LIST;
import static net.kyma.EventType.DATA_REFRESH;
import static net.kyma.EventType.DATA_REFRESH_PATHS;
import static net.kyma.EventType.DATA_REMOVE_ITEM;
import static net.kyma.EventType.DATA_REMOVE_PATH;
import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.DATA_STORE_LIST;
import static net.kyma.EventType.RET_DOC_CONVERTER;
import static net.kyma.EventType.RET_INDEX_WRITER;
import static net.kyma.EventType.SHOW_ALERT;
import static net.kyma.data.QueryUtils.termForPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.BusRequestException;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Bits;
import pl.khuzzuk.functions.ForceGate;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class DataIndexer implements Loadable
{
   private final Bus<EventType> bus;
   private IndexWriter writer;
   private DocConverter docConverter;
   private Set<String> indexedPaths;
   private ForceGate initializer;
   private static final String READ_DATA_ERROR_MESSAGE = "Cannot read data";

   @Override
   public void load()
   {
      initializer = ForceGate.of(2, this::afterPropertiesSet);
      bus.subscribingFor(RET_DOC_CONVERTER).accept(this::setDocConverter).subscribe();
      bus.subscribingFor(RET_INDEX_WRITER).accept(this::setWriter).subscribe();
   }

   private void setWriter(IndexWriter writer)
   {
      this.writer = writer;
      initializer.on();
   }

   private void setDocConverter(DocConverter docConverter)
   {
      this.docConverter = docConverter;
      initializer.on();
   }

   private void afterPropertiesSet()
   {
      bus.subscribingFor(CLOSE).then(this::close).subscribe();
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

   private synchronized void index(@NonNull Collection<SoundFile> files)
   {
      if (files.isEmpty()) {
         return;
      }

      String path = files.iterator().next().getIndexedPath();
      indexedPaths.stream().filter(path::startsWith).findAny()
            .ifPresent(indexed -> files.forEach(f -> f.setIndexedPath(indexed)));

      files.forEach(this::indexSingleEntity);
      commit();
      refreshIndexedPaths();
   }

   private void indexSingleEntity(@NonNull SoundFile soundFile)
   {
      try (DirectoryReader reader = DirectoryReader.open(writer))
      {
         IndexSearcher searcher = new IndexSearcher(reader);
         if (isNew(soundFile, searcher))
         {
            writer.addDocument(docConverter.docFrom(soundFile));
         }
         else
         {
            normalizeIndexingPath(soundFile, searcher);
            writer.updateDocument(termForPath(soundFile.getPath()), docConverter.docFrom(soundFile));
         }
      }
      catch (IOException e)
      {
         log.error("Indexing occurred error", e);
      }
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
      }
      catch (IOException e)
      {
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
      }
      catch (IOException e) {
         log.error(READ_DATA_ERROR_MESSAGE, e);
      }

      bus.message(DATA_CONVERT_FROM_DOC).withResponse(DATA_REFRESH).withContent(documents).send();
   }

   private boolean isNew(SoundFile soundFile, IndexSearcher searcher)
   {
      try
      {
         return searcher.search(new TermQuery(termForPath(soundFile.getPath())), 1)
               .scoreDocs.length == 0;
      }
      catch (IOException e)
      {
         log.error(READ_DATA_ERROR_MESSAGE, e);
      }
      return false;
   }

   private void remove(Collection<SoundFile> soundFiles)
   {
      soundFiles.forEach(this::remove);
      commit();
   }

   private void remove(SoundFile soundFile) {
      try
      {
         writer.deleteDocuments(termForPath(soundFile.getPath()));
      }
      catch (IOException e)
      {
         log.error("Cannot delete document, problem with accessing the index files");
         log.error(e);
      }
   }

   private void removePath(PathQueryParameters parameters) {
      try {
         long deleteDocuments = writer.deleteDocuments(parameters.createQuery());
         commit();
         log.debug("Deleted documents: ");
         log.debug(deleteDocuments);
      } catch (IOException e) {
         log.error("Cannot remove index entries for path", e);
         bus.message(SHOW_ALERT).withContent("Indexing error").send();
         throw new BusRequestException(e);
      }
   }

   private void refreshIndexedPaths() {
      bus.message(DATA_INDEX_GET_DISTINCT)
            .withResponse(DATA_INDEX_GET_DIRECTORIES)
            .withContent(SupportedField.INDEXED_PATH)
            .send();
      bus.message(DATA_GET_PATHS).withResponse(DATA_REFRESH_PATHS).send();
   }

   private void commit() {
      try {
         writer.commit();
      } catch (IOException e) {
         log.error("Error during indexing database");
         bus.message(SHOW_ALERT).withContent("Indexing access problem").send();
      }
   }

   private void close()
   {
      try
      {
         writer.close();
      }
      catch (IOException e)
      {
         log.error("error on closing database", e);
      }
      if (writer.isOpen())
      {
         try
         {
            Thread.sleep(100);
         }
         catch (InterruptedException e)
         {
            log.error("Error during close operation, retry", e);
            Thread.currentThread().interrupt();
            close();
         }
         close();
      }
      //TODO refactoring, change place where bus is closed
      bus.closeBus();
   }
}
