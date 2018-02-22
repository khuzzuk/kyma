package net.kyma.data;

import static net.kyma.EventType.CLOSE;
import static net.kyma.EventType.DATA_CONVERT_FROM_DOC;
import static net.kyma.EventType.DATA_INDEX_GET_ALL;
import static net.kyma.EventType.DATA_INDEX_GET_DIRECTORIES;
import static net.kyma.EventType.DATA_INDEX_GET_DISTINCT;
import static net.kyma.EventType.DATA_INDEX_ITEM;
import static net.kyma.EventType.DATA_INDEX_LIST;
import static net.kyma.EventType.DATA_REMOVE_ITEM;
import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.DATA_STORE_LIST;
import static net.kyma.data.QueryUtils.termForPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class DataIndexer implements Loadable
{
   private final Bus<EventType> bus;
   private final IndexWriter writer;
   private final DocConverter docConverter;
   private Set<String> indexedPaths;

   @Override
   public void load()
   {
      bus.setReaction(CLOSE, this::close);
      bus.setReaction(DATA_INDEX_LIST, this::index);
      bus.setReaction(DATA_STORE_LIST, this::index);
      bus.setReaction(DATA_INDEX_ITEM, this::indexSingleEntity);
      bus.setReaction(DATA_STORE_ITEM, this::indexSingleEntity);
      bus.setResponse(DATA_INDEX_GET_ALL, this::getAll);
      bus.<Collection<SoundFile>>setReaction(DATA_REMOVE_ITEM, this::remove);
      bus.<Set<String>>setReaction(DATA_INDEX_GET_DIRECTORIES, paths -> indexedPaths = paths);
      refreshIndexedPaths();
   }

   private synchronized void index(@NonNull Collection<SoundFile> files)
   {
      if (files.isEmpty())
      {
         return;
      }

      String path = files.iterator().next().getIndexedPath();
      indexedPaths.stream().filter(path::startsWith).findAny()
            .ifPresent(indexed -> files.forEach(f -> f.setIndexedPath(indexed)));

      files.forEach(this::indexSingleEntity);
      commit();
      bus.sendMessage(DATA_INDEX_GET_ALL, DATA_CONVERT_FROM_DOC);
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
         log.error("Indexing occurred error");
         log.error(e);
      }
   }

   private void normalizeIndexingPath(SoundFile soundFile, IndexSearcher searcher)
   {
      String previousPath = soundFile.getIndexedPath();
      try
      {
         ScoreDoc[] scoreDocs = searcher.search(new TermQuery(termForPath(soundFile.getPath())), 1).scoreDocs;
         if (scoreDocs.length == 1)
         {
            previousPath = searcher.doc(scoreDocs[0].doc, Collections.singleton(SupportedField.INDEXED_PATH.getName()))
                  .get(SupportedField.INDEXED_PATH.getName());
         }
      }
      catch (IOException e)
      {
         log.error("Cannot read data");
         log.error(e);
      }
      soundFile.setIndexedPath(previousPath);
   }

   private synchronized Collection<Document> getAll()
   {
      Collection<Document> documents = new ArrayList<>();
      try
      {
         DirectoryReader reader = DirectoryReader.open(writer);
         int maxDoc = reader.maxDoc();
         Bits liveDocs = MultiFields.getLiveDocs(reader);
         for (int i = 0; i < maxDoc; i++)
         {
            if (liveDocs == null || liveDocs.get(i))
            {
               documents.add(reader.document(i));
            }
         }
         reader.close();
      }
      catch (IOException e)
      {
         log.error("Cannot read data");
         log.error(e);
      }

      return documents;
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
         log.error("Cannot read data");
         log.error(e);
      }
      return false;
   }

   private void remove(Collection<SoundFile> soundFiles)
   {
      soundFiles.forEach(this::remove);
      commit();
   }

   private void remove(SoundFile soundFile)
   {
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

   private void refreshIndexedPaths()
   {
      bus.send(DATA_INDEX_GET_DISTINCT, DATA_INDEX_GET_DIRECTORIES, SupportedField.INDEXED_PATH);
   }

   private void commit()
   {
      try
      {
         writer.commit();
      }
      catch (IOException e)
      {
         log.error("Error during indexing database");
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
         log.error("error on closing database");
         log.error(e);
      }
      if (writer.isOpen())
      {
         try
         {
            Thread.sleep(100);
         }
         catch (InterruptedException e)
         {
            log.error("Error during close operation, retry");
         }
         close();
      }
      //TODO refactoring, change place where bus is closed
      bus.closeBus();
   }
}
