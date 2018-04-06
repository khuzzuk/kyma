package net.kyma.gui;

import static net.kyma.EventType.CLOSE;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FRAME;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FULLSCREEN;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_MAXIMIZED;

import java.awt.Rectangle;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.gui.controllers.ControllerDistributor;
import net.kyma.gui.controllers.MainController;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class MainWindow extends Stage {
    private final Bus<EventType> bus;
    private final ControllerDistributor controllerDistributor;
    private final MainController mainController;

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
            log.fatal("Error during gui initialization", e);
        }
        setOnCloseRequest(event -> onClose());
    }

    private void onClose() {
        if (isFullScreen()) {
            bus.message(PROPERTIES_STORE_WINDOW_FULLSCREEN).withContent(Boolean.TRUE).send();
        } else if (isMaximized()) {
            bus.message(PROPERTIES_STORE_WINDOW_FULLSCREEN).withContent(Boolean.FALSE).send();
            bus.message(PROPERTIES_STORE_WINDOW_MAXIMIZED).withContent(Boolean.TRUE).send();
        } else {
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(getX(), getY(), getWidth(), getHeight());
            bus.message(PROPERTIES_STORE_WINDOW_FRAME).withContent(rectangle).send();
            bus.message(PROPERTIES_STORE_WINDOW_FULLSCREEN).withContent(Boolean.FALSE).send();
            bus.message(PROPERTIES_STORE_WINDOW_MAXIMIZED).withContent(Boolean.FALSE).send();
        }
        bus.message(CLOSE).send();
    }
}
