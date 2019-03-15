package net.kyma.gui;

import javafx.scene.control.ProgressIndicator;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import pl.khuzzuk.messaging.Bus;

import java.awt.Rectangle;
import java.io.File;
import java.util.Optional;

import static net.kyma.EventType.DATA_INDEXING_AMOUNT;
import static net.kyma.EventType.DATA_INDEXING_FINISH;
import static net.kyma.EventType.DATA_INDEXING_PROGRESS;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
import static net.kyma.EventType.DATA_REFRESH;
import static net.kyma.EventType.GUI_WINDOW_GET_FRAME;
import static net.kyma.EventType.GUI_WINDOW_IS_FULLSCREEN;
import static net.kyma.EventType.GUI_WINDOW_IS_MAXIMIZED;
import static net.kyma.EventType.GUI_WINDOW_SET_FRAME;
import static net.kyma.EventType.GUI_WINDOW_SET_FULLSCREEN;
import static net.kyma.EventType.GUI_WINDOW_SET_MAXIMIZED;
import static net.kyma.EventType.PLAYLIST_ADD_FILE;
import static net.kyma.EventType.PLAYLIST_ADD_SOUND;

@Log4j2
@RequiredArgsConstructor
public class MainController {
    private final Bus<EventType> bus;
    private double maxProgress;

    @Setter
    private ProgressIndicator indicator;
    @Setter
    private Stage stage;

    public void initialize() {
        bus.subscribingFor(DATA_REFRESH).onFXThread().accept(o -> indicator.setVisible(false)).subscribe();
        bus.subscribingFor(DATA_INDEX_DIRECTORY).onFXThread().then(this::showIndicator).subscribe();
        bus.subscribingFor(DATA_INDEXING_AMOUNT).onFXThread()
                .then(this::showIndicator)
                .accept((Integer max) -> maxProgress = max.doubleValue()).subscribe();
        bus.subscribingFor(DATA_INDEXING_PROGRESS).onFXThread()
              .<Number>accept(num -> indicator.setProgress(num.doubleValue() / maxProgress)).subscribe();
        bus.subscribingFor(DATA_INDEXING_FINISH).onFXThread().then(() -> indicator.setVisible(false)).subscribe();
        bus.subscribingFor(GUI_WINDOW_SET_FULLSCREEN).onFXThread().then(() -> stage.setFullScreen(true)).subscribe();
        bus.subscribingFor(GUI_WINDOW_SET_MAXIMIZED).onFXThread().then(() -> stage.setMaximized(true)).subscribe();
        bus.subscribingFor(GUI_WINDOW_SET_FRAME).onFXThread().accept(this::resize).subscribe();

        bus.message(GUI_WINDOW_GET_FRAME).send();
    }

    private void showIndicator() {
        indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        indicator.setVisible(true);
    }

    void openFile() {
        Optional.ofNullable(getFile(new FileChooser.ExtensionFilter("Pliki dźwiękowe", "*.mp3")))
                .ifPresent(file -> bus.message(PLAYLIST_ADD_FILE).withResponse(PLAYLIST_ADD_SOUND).withContent(file).send());
    }

    void indexDirectory() {
        Optional.ofNullable(getFile()).ifPresent(file -> bus.message(DATA_INDEX_DIRECTORY).withContent(file).send());
    }

    private static File getFile(FileChooser.ExtensionFilter filter) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("wybór pliku");
        chooser.getExtensionFilters().addAll(filter);
        return chooser.showOpenDialog(null);
    }

    private static File getFile() {
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
        bus.message(GUI_WINDOW_IS_FULLSCREEN).send();
        bus.message(GUI_WINDOW_IS_MAXIMIZED).send();
    }
}
