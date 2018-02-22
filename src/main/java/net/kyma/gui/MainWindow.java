package net.kyma.gui;

import static net.kyma.EventType.CLOSE;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FRAME;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FULLSCREEN;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_MAXIMIZED;

import java.awt.*;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import net.kyma.EventType;
import net.kyma.gui.controllers.ControllerDistributor;
import net.kyma.gui.controllers.MainController;
import pl.khuzzuk.messaging.Bus;

public class MainWindow extends Stage {
    private Bus<EventType> bus;
    private ControllerDistributor controllerDistributor;
    private MainController mainController;

    public MainWindow(Bus<EventType> bus, ControllerDistributor controllerDistributor, MainController mainController) {
        super(StageStyle.DECORATED);
        this.bus = bus;
        initModality(Modality.WINDOW_MODAL);
        this.controllerDistributor = controllerDistributor;
        this.mainController = mainController;
    }

    public void initMainWindow(Window parent) {
        mainController.setStage(this);
        initOwner(parent);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainWindow.fxml"));
        loader.setControllerFactory(controllerDistributor);
        try {
            setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setOnCloseRequest(this::onClose);
    }

    private void onClose(WindowEvent e) {
        if (isFullScreen()) {
            bus.send(PROPERTIES_STORE_WINDOW_FULLSCREEN, Boolean.TRUE);
        } else if (isMaximized()) {
            bus.send(PROPERTIES_STORE_WINDOW_FULLSCREEN, Boolean.FALSE);
            bus.send(PROPERTIES_STORE_WINDOW_MAXIMIZED, Boolean.TRUE);
        } else {
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(getX(), getY(), getWidth(), getHeight());
            bus.send(PROPERTIES_STORE_WINDOW_FRAME, rectangle);
            bus.send(PROPERTIES_STORE_WINDOW_FULLSCREEN, Boolean.FALSE);
            bus.send(PROPERTIES_STORE_WINDOW_MAXIMIZED, Boolean.FALSE);
        }
        bus.send(CLOSE);
    }
}
