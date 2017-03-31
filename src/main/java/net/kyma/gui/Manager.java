package net.kyma.gui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;
import net.kyma.bindings.BusModule;
import net.kyma.bindings.ControllersModule;

public class Manager extends Application {
    public static Injector injector;
    public static void main(String[] args) {
        injector = Guice.createInjector(new ControllersModule(), new BusModule());
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindow window = injector.getInstance(MainWindow.class);
        window.initMainWindow(primaryStage);
        window.show();
    }
}
