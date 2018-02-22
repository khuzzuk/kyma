package net.kyma.gui.controllers;

import static net.kyma.EventType.DATA_INDEXING_AMOUNT;
import static net.kyma.EventType.DATA_INDEXING_FINISH;
import static net.kyma.EventType.DATA_INDEXING_PROGRESS;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
import static net.kyma.EventType.DATA_REFRESH;
import static net.kyma.EventType.GUI_WINDOW_SETTINGS;
import static net.kyma.EventType.GUI_WINDOW_SET_FRAME;
import static net.kyma.EventType.GUI_WINDOW_SET_FULLSCREEN;
import static net.kyma.EventType.GUI_WINDOW_SET_MAXIMIZED;
import static net.kyma.EventType.PLAYLIST_ADD_FILE;
import static net.kyma.EventType.PLAYLIST_ADD_SOUND;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import pl.khuzzuk.messaging.Bus;

@SuppressWarnings("WeakerAccess")
@Log4j2
public class MainController implements Initializable {
    @FXML
    private SplitPane mainPane;
    @FXML
    private GridPane managerPane;
    @FXML
    private GridPane playerPane;
    private Bus<EventType> bus;
    private ManagerPaneController managerPaneController;
    private ProgressIndicator indicator;
    private double maxProgress;
    @Setter
    private Stage stage;

    public MainController(Bus<EventType> bus, ManagerPaneController managerPaneController)
    {
        this.bus = bus;
        this.managerPaneController = managerPaneController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bus.setFXReaction(DATA_REFRESH, o -> mainPane.getItems().remove(indicator));
        bus.setFXReaction(DATA_INDEXING_AMOUNT, this::showIndicator);
        bus.<Number>setFXReaction(DATA_INDEXING_PROGRESS, n -> indicator.setProgress(n.doubleValue() / maxProgress));
        bus.setFXReaction(DATA_INDEXING_FINISH, () -> mainPane.getItems().remove(indicator));
        bus.setFXReaction(GUI_WINDOW_SET_FULLSCREEN, () -> {
            stage.setFullScreen(true); setBackground();
        });
        bus.setFXReaction(GUI_WINDOW_SET_MAXIMIZED, () -> {
            stage.setMaximized(true); setBackground();
        });
        bus.setFXReaction(GUI_WINDOW_SET_FRAME, this::resize);

        bus.send(GUI_WINDOW_SETTINGS);
        managerPaneController.resizeFor(mainPane);
    }

    private void showIndicator(int maxProgress)
    {
        this.maxProgress = maxProgress;
        indicator = new ProgressIndicator();
        mainPane.getItems().add(indicator);
        indicator.setVisible(true);
    }

    private void setBackground() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        mainPane.setBackground(new Background(new BackgroundImage(new Image("/css/classic-background.png"),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(screenSize.getWidth(), screenSize.getHeight(),
                        false, false, false, false))));
    }

    @FXML
    private void openFile() {
        Optional.ofNullable(getFile(new FileChooser.ExtensionFilter("Pliki dźwiękowe", "*.mp3")))
                .ifPresent(f -> bus.send(PLAYLIST_ADD_FILE, PLAYLIST_ADD_SOUND, f));
    }

    @FXML
    private void indexCatalogue() {
        Optional.ofNullable(getFile()).ifPresent(f -> {
            bus.send(DATA_INDEX_DIRECTORY, f);
        });
    }

    private File getFile(FileChooser.ExtensionFilter filter) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("wybór pliku");
        chooser.getExtensionFilters().addAll(filter);
        return chooser.showOpenDialog(null);
    }

    private File getFile() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("wybór katalogu");
        return chooser.showDialog(null);
    }

    private void resize(Rectangle r) {
        stage.setMaximized(false);
        stage.setX(r.getX());
        stage.setY(r.getY());
        stage.setWidth(r.getWidth());
        stage.setHeight(r.getHeight());
        setBackground();
    }
}
