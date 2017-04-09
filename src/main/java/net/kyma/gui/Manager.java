package net.kyma.gui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.kyma.BusModule;
import net.kyma.data.DataIndexer;
import net.kyma.data.DatabaseModule;
import pl.khuzzuk.messaging.Bus;

import java.util.Properties;

@Log4j2
public class Manager extends Application {
    private static Injector injector;
    private static Bus bus;
    private static Properties messages;
    public static void main(String[] args) {
        injector = Guice.createInjector(new ControllersModule(), new BusModule(), new DatabaseModule());
        injector.getInstance(DataIndexer.class).init();
        bus = injector.getInstance(Bus.class);
        messages = injector.getInstance(Key.get(Properties.class, Names.named("messages")));
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindow window = injector.getInstance(MainWindow.class);
        window.initMainWindow(primaryStage);
        window.setOnCloseRequest(e -> bus.send(messages.getProperty("close")));
        window.show();
    }
}
