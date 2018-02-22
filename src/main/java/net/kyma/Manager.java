package net.kyma;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.kyma.data.DataIndexer;
import net.kyma.data.DataReader;
import net.kyma.data.DirectoryIndexer;
import net.kyma.data.DocConverter;
import net.kyma.data.FileCleaner;
import net.kyma.data.MetadataIndexer;
import net.kyma.data.PlayCounter;
import net.kyma.data.SoundFileConverter;
import net.kyma.gui.MainWindow;
import net.kyma.gui.TableColumnFactory;
import net.kyma.gui.controllers.ContentView;
import net.kyma.gui.controllers.ControllerDistributor;
import net.kyma.gui.controllers.MainController;
import net.kyma.gui.controllers.ManagerPaneController;
import net.kyma.gui.controllers.PlayerPaneController;
import net.kyma.player.PlaybackTimer;
import net.kyma.player.PlayerManager;
import net.kyma.player.Playlist;
import net.kyma.properties.PropertiesLoader;
import net.kyma.properties.PropertiesManager;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class Manager extends Application {

    private static MainWindow mainWindow;

    public static void main(String[] args) throws IOException
    {
        Bus<EventType> bus = Bus.initializeBus(EventType.class, false);

        //GUI
        TableColumnFactory columnFactory = new TableColumnFactory(bus);
        ManagerPaneController managerPaneController = new ManagerPaneController(bus, columnFactory);
        MainController mainController = new MainController(bus, managerPaneController);

        PlaybackTimer playbackTimer = new PlaybackTimer(bus);
        playbackTimer.load();
        PlayerPaneController playerPaneController = new PlayerPaneController(bus, playbackTimer);

        ObjectContainer container = new ObjectContainer(bus);
        ContentView contentView = new ContentView(bus, columnFactory);
        container.putToContainer(EventType.RET_CONTENT_VIEW, contentView);

        ControllerDistributor controllerDistributor = new ControllerDistributor(
              mainController,
              playerPaneController,
              managerPaneController,
              contentView);

        Platform.runLater(() -> mainWindow = new MainWindow(bus, controllerDistributor, mainController));

        //Properties
        PropertiesLoader propertiesLoader = new PropertiesLoader();
        propertiesLoader.load();
        PropertiesManager propertiesManager = new PropertiesManager(bus, propertiesLoader);
        propertiesManager.load();

        //Data
        Directory directory = new NIOFSDirectory(Paths.get("index/"));
        IndexWriterConfig config = new IndexWriterConfig();
        config.setRAMBufferSizeMB(64);
        IndexWriter writer = new IndexWriter(directory, config);
        SoundFileConverter soundFileConverter = new SoundFileConverter(bus);

        DataReader dataReader = new DataReader(bus, writer);
        dataReader.load();
        DataIndexer dataIndexer = new DataIndexer(bus, writer, new DocConverter());
        dataIndexer.load();
        DirectoryIndexer directoryIndexer = new DirectoryIndexer(bus, soundFileConverter);
        directoryIndexer.load();
        FileCleaner fileCleaner = new FileCleaner(bus);
        fileCleaner.load();
        PlayCounter playCounter = new PlayCounter(bus);
        playCounter.load();
        MetadataIndexer metadataIndexer = new MetadataIndexer(bus);
        metadataIndexer.load();

        //Player
        Playlist playlist = new Playlist(bus);
        playlist.load();
        PlayerManager playerManager = new PlayerManager(bus, playbackTimer);
        playerManager.load();

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainWindow.initMainWindow(primaryStage);
        mainWindow.show();
    }
}
