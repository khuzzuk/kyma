package net.kyma.gui.controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.extern.log4j.Log4j2;
import net.kyma.data.SoundFileConverter;
import net.kyma.gui.BaseElement;
import net.kyma.gui.RootElement;
import net.kyma.gui.SoundElement;
import net.kyma.dm.SoundFile;
import net.kyma.gui.TableColumnFactory;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@Singleton
@Log4j2
public class MainController implements Initializable {
    @FXML
    private SplitPane mainPane;
    @FXML
    private GridPane managerPane;
    @FXML
    private GridPane playerPane;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    private ProgressIndicator indicator;
    private double maxProgress;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bus.setGuiReaction(messages.getProperty("data.view.refresh"), o -> mainPane.getItems().remove(indicator));
        bus.<Number>setReaction(messages.getProperty("data.index.gui.amount"), n -> maxProgress = n.doubleValue());
        bus.<Number>setGuiReaction(messages.getProperty("data.index.gui.progress"), n -> indicator.setProgress(n.doubleValue() / maxProgress));
        bus.setGuiReaction(messages.getProperty("data.index.gui.finish"), () -> mainPane.getItems().remove(indicator));
    }

    @FXML
    private void openFile() {
        Optional.ofNullable(getFile(new FileChooser.ExtensionFilter("Pliki dźwiękowe", "*.mp3")))
                .ifPresent(f -> bus.send(messages.getProperty("playlist.add.file"), messages.getProperty("playlist.add.sound"), f));
    }

    @FXML
    private void indexCatalogue() {
        Optional.ofNullable(getFile()).ifPresent(f -> {
            indicator = new ProgressIndicator();
            mainPane.getItems().add(indicator);
            indicator.setVisible(true);
            bus.send(messages.getProperty("data.index.directory"), f);
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
}
