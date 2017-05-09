package net.kyma.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.*;
import javafx.stage.Window;
import net.kyma.gui.controllers.ControllerDistributor;
import net.kyma.gui.controllers.MainController;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;
import java.io.IOException;
import java.util.Properties;

public class MainWindow extends Stage {
    private final ControllerDistributor controllerDistributor;
    private final MainController mainController;
    private Bus bus;
    private Properties messages;

    @Inject
    public MainWindow(ControllerDistributor controllerDistributor, MainController mainController,
                      Bus bus, @Named("messages") Properties messages) {
        super(StageStyle.DECORATED);
        initModality(Modality.WINDOW_MODAL);
        this.controllerDistributor = controllerDistributor;
        this.mainController = mainController;
        this.bus = bus;
        this.messages = messages;
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
            bus.send(messages.getProperty("properties.window.store.fullScreen"), Boolean.TRUE);
        } else if (isMaximized()) {
            bus.send(messages.getProperty("properties.window.store.fullScreen"), Boolean.FALSE);
            bus.send(messages.getProperty("properties.window.store.maximized"), Boolean.TRUE);
        } else {
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(getX(), getY(), getWidth(), getHeight());
            bus.send(messages.getProperty("properties.window.store.frame"), rectangle);
            bus.send(messages.getProperty("properties.window.store.fullScreen"), Boolean.FALSE);
            bus.send(messages.getProperty("properties.window.store.maximized"), Boolean.FALSE);
        }
        bus.send("close");
    }
}
