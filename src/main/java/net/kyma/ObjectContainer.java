package net.kyma;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.data.DataIndexer;
import net.kyma.data.DataReader;
import net.kyma.data.DirectoryIndexer;
import net.kyma.data.DocConverter;
import net.kyma.data.FileCleaner;
import net.kyma.data.MetadataIndexer;
import net.kyma.data.PlayCounter;
import net.kyma.data.SoundFileConverter;
import net.kyma.gui.communicate.Alert;
import net.kyma.player.PlayerManager;
import net.kyma.player.Playlist;
import net.kyma.properties.PropertiesManager;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
@Log4j2
public class ObjectContainer
{
   private final Bus<EventType> bus;
   private Collection<Loadable> loadables;
   private Collection<Pair<EventType, Object>> toSend;

   void createContainer()
   {
      loadables = new ArrayList<>(32);
      toSend = new ArrayList<>(32);
      initGraph();
      loadables.forEach(Loadable::load);
      toSend.forEach(pair -> bus.message(pair.getKey()).withContent(pair.getValue()).send());
   }

   void putToContainer(EventType retrieveEvent, Object object)
   {
      toSend.add(new Pair<>(retrieveEvent, object));
   }

   @SuppressWarnings("unused")
   private void putToContainer(EventType retrieveEvent, Loadable loadable)
   {
      toSend.add(new Pair<>(retrieveEvent, loadable));
      loadables.add(loadable);
   }

   private void initGraph()
   {
      initJsonParser();
      initProperties();
      initDataAccess();
      initDataIndex();
      initPlayer();
      initGuiDependency();
   }

   private void initJsonParser()
   {
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      putToContainer(EventType.RET_OBJECT_MAPPER, mapper);
   }

   private void initProperties()
   {
      loadables.add(new PropertiesManager(bus));
   }

   private void initDataAccess() {
      try {
         Directory directory = new NIOFSDirectory(Paths.get("index/")); //NOSONAR
         IndexWriterConfig config = new IndexWriterConfig();
         config.setRAMBufferSizeMB(64);
         IndexWriter writer = new IndexWriter(directory, config);
         putToContainer(EventType.RET_INDEX_WRITER, writer);

         SoundFileConverter soundFileConverter = new SoundFileConverter(bus);
         putToContainer(EventType.RET_SOUND_FILE_CONVERTER, soundFileConverter);
      } catch (IOException e) {
         log.fatal("cannot start index", e);
      }
   }

   private void initDataIndex()
   {
      putToContainer(EventType.RET_DOC_CONVERTER, new DocConverter());
      loadables.add(new DataReader(bus));
      loadables.add(new DataIndexer(bus));
      loadables.add(new DirectoryIndexer(bus));
      loadables.add(new FileCleaner(bus));
      loadables.add(new MetadataIndexer(bus));
   }

   private void initPlayer() {
      loadables.add(new PlayCounter(bus));
      loadables.add(new Playlist(bus));
      loadables.add(new PlayerManager(bus));
   }

   private void initGuiDependency() {
      loadables.add(new Alert(bus));
   }
}
