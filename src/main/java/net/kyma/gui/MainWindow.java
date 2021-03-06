package net.kyma.gui;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.gui.content.ContentPane;
import net.kyma.gui.manager.ManagerPane;
import net.kyma.gui.player.PlayerPane;
import pl.khuzzuk.messaging.Bus;

import java.awt.Rectangle;

import static net.kyma.EventType.CLOSE;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FRAME;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FULLSCREEN;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_MAXIMIZED;

@Log4j2
public class MainWindow extends Stage {
    private final Bus<EventType> bus;
    private final ControllerDistributor controllerDistributor;

    public MainWindow(Bus<EventType> bus, ControllerDistributor controllerDistributor) {
        super(StageStyle.DECORATED);
        this.bus = bus;
        initModality(Modality.WINDOW_MODAL);
        this.controllerDistributor = controllerDistributor;
    }

    public void initMainWindow(Window parent) {
        controllerDistributor.getController().setStage(this);
        initOwner(parent);
        PlayerPane playerPane = new PlayerPane(controllerDistributor.getPlayerPaneController());
        ContentPane contentPane = new ContentPane(controllerDistributor.getContentPaneController());
        ManagerPane managerPane = new ManagerPane(controllerDistributor.getManagerPaneController(), contentPane);
        MainPane mainPane = new MainPane(managerPane, playerPane, controllerDistributor.getController(), this);
        setScene(ClassicTheme.style(new Scene(mainPane)));
        setOnCloseRequest(event -> onClose());
    }

    private void onClose() {
        Rectangle rectangle = new Rectangle();
        rectangle.setRect(getX(), getY(), getWidth(), getHeight());
        bus.message(PROPERTIES_STORE_WINDOW_FRAME).withContent(rectangle).send();
        bus.message(PROPERTIES_STORE_WINDOW_FULLSCREEN).withContent(isFullScreen()).send();
        bus.message(PROPERTIES_STORE_WINDOW_MAXIMIZED).withContent(isMaximized()).send();
        bus.message(CLOSE).send();
    }
}
