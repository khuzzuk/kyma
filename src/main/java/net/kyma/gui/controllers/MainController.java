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

import java.awt.Rectangle;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import pl.khuzzuk.messaging.Bus;

@SuppressWarnings("WeakerAccess")
@Log4j2
@RequiredArgsConstructor
public class MainController implements Initializable {
    @FXML
    private SplitPane mainPane;
    @FXML
    private GridPane managerPane;
    @FXML
    private GridPane playerPane;

    private final Bus<EventType> bus;
    private final ManagerPaneController managerPaneController;
    private ProgressIndicator indicator;
    private double maxProgress;
    @Setter
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bus.subscribingFor(DATA_REFRESH).onFXThread().accept(o -> mainPane.getItems().remove(indicator)).subscribe();
        bus.subscribingFor(DATA_INDEX_DIRECTORY).onFXThread().then(this::showIndicator).subscribe();
        bus.subscribingFor(DATA_INDEXING_AMOUNT).onFXThread().accept(max -> maxProgress = (double) max).subscribe();
        bus.subscribingFor(DATA_INDEXING_PROGRESS).onFXThread()
              .<Number>accept(n -> indicator.setProgress(n.doubleValue() / maxProgress)).subscribe();
        bus.subscribingFor(DATA_INDEXING_FINISH).onFXThread()
              .then(() -> mainPane.getItems().remove(indicator)).subscribe();
        bus.subscribingFor(GUI_WINDOW_SET_FULLSCREEN).onFXThread().then(() -> stage.setFullScreen(true)).subscribe();
        bus.subscribingFor(GUI_WINDOW_SET_MAXIMIZED).onFXThread().then(() -> stage.setMaximized(true)).subscribe();
        bus.subscribingFor(GUI_WINDOW_SET_FRAME).onFXThread().accept(this::resize).subscribe();

        bus.message(GUI_WINDOW_SETTINGS).send();
        managerPaneController.resizeFor(mainPane);
    }

    private void showIndicator()
    {
        indicator = new ProgressIndicator();
        mainPane.getItems().add(indicator);
        indicator.setVisible(true);
    }

    @FXML
    private void openFile() {
        Optional.ofNullable(getFile(new FileChooser.ExtensionFilter("Pliki dźwiękowe", "*.mp3")))
                .ifPresent(f -> bus.message(PLAYLIST_ADD_FILE).withResponse(PLAYLIST_ADD_SOUND).withContent(f).send());
    }

    @FXML
    private void indexCatalogue() {
        Optional.ofNullable(getFile()).ifPresent(f -> bus.message(DATA_INDEX_DIRECTORY).withContent(f).send());
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
    }
}
