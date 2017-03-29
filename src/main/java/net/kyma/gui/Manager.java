package net.kyma.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Manager extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindow window = new MainWindow(primaryStage);
        window.show();
    }
}
