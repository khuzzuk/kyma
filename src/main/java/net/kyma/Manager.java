package net.kyma;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.kyma.data.DataIndexer;
import net.kyma.data.DatabaseModule;
import net.kyma.data.DirectoryIndexer;
import net.kyma.data.MetadataIndexer;
import net.kyma.gui.ControllersModule;
import net.kyma.gui.MainWindow;
import net.kyma.gui.SoundFileBulkEditor;
import net.kyma.gui.SoundFileEditor;
import net.kyma.player.PlayerManager;
import net.kyma.player.Playlist;
import net.kyma.properties.ColumnManager;
import net.kyma.properties.PropertiesManager;
import net.kyma.properties.PropertiesModule;
import pl.khuzzuk.messaging.Bus;

import java.util.Properties;

@Log4j2
public class Manager extends Application {
    private static Injector injector;
    private static Bus bus;
    private static Properties messages;

    public static void main(String[] args) {
        injector = Guice.createInjector(new ControllersModule(), new BusModule(),
                new DatabaseModule(), new PropertiesModule());
        injector.getInstance(DataIndexer.class).init();
        injector.getInstance(Playlist.class).init();
        injector.getInstance(PlayerManager.class).init();
        injector.getInstance(DirectoryIndexer.class).init();
        injector.getInstance(MetadataIndexer.class).init();
        injector.getInstance(PropertiesManager.class).init();
        injector.getInstance(SoundFileEditor.class).init();
        injector.getInstance(SoundFileBulkEditor.class).init();
        injector.getInstance(ColumnManager.class).init();

        new Image("/css/background.jpg");

        bus = injector.getInstance(Bus.class);
        messages = injector.getInstance(Key.get(Properties.class, Names.named("messages")));

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindow window = injector.getInstance(MainWindow.class);
        window.initMainWindow(primaryStage);
        window.show();
    }
}
