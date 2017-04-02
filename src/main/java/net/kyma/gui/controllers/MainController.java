package net.kyma.gui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

@Singleton
public class MainController implements Initializable {
    @FXML
    private TableView<SoundFile> playlist;
    @FXML
    private GridPane playerPane;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bus.<SoundFile>setReaction(messages.getProperty("playlist.add.sound"), playlist.getItems()::add);
        initPlaylistView();
    }

    private void initPlaylistView() {
        playlist.getColumns().clear();
        TableColumn<SoundFile, String> title = new TableColumn<>("Tytuł");
        title.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTitle()));
        playlist.getColumns().add(title);
    }

    @FXML
    private void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("wybór pliku");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Pliki dźwiękowe", "*.mp3"));
        Optional.ofNullable(chooser.showOpenDialog(null))
                .ifPresent(f -> bus.send(messages.getProperty("playlist.add.file"), messages.getProperty("playlist.add.sound"), f));
    }
}
