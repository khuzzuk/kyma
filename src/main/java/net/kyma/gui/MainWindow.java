package net.kyma.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.gui.controllers.ControllerDistributor;
import net.kyma.gui.manager.ManagerPane;
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
        ManagerPane managerPane = new ManagerPane(controllerDistributor.getManagerPaneController());
        MainPane mainPane = new MainPane(managerPane, controllerDistributor.getController(), this);
        SplitPane splitPane = new SplitPane(new Button("BUTTON"), new Button("1"));
        splitPane.setDividerPositions(0.8d);
        splitPane.setMaxWidth(Double.MAX_VALUE);
        splitPane.setMinWidth(1800d);
        splitPane.setBackground(new Background(new BackgroundFill(Color.VIOLET, null, null)));
        HBox hBox = new HBox(splitPane);
        hBox.setFillHeight(true);
        HBox.setHgrow(hBox, Priority.ALWAYS);
        hBox.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        setMaxHeight(Double.MAX_VALUE);
        setScene(new Scene(hBox));
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
