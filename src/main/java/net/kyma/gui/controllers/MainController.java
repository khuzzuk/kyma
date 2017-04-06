package net.kyma.gui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.*;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@Log4j2
public class MainController implements Initializable {
    @FXML
    private TreeView<BaseElement> filesList;
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
        bus.setReaction(messages.getProperty("data.view.refresh"), this::fillTreeView);
        initPlaylistView();
    }

    private void initPlaylistView() {
        playlist.getColumns().clear();
        TableColumn<SoundFile, String> title = new TableColumn<>("Tytuł");
        title.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTitle()));
        playlist.getColumns().add(title);
    }

    private void fillTreeView(Collection<SoundFile> sounds) {
        BaseElement root = new RootElement();
        List<SoundFile> soundFiles = sounds.stream().sorted().collect(Collectors.toList());
        for (SoundFile f : soundFiles) {
            String[] path = f.getPath().split("[\\\\/]+");
            if (path.length == 0) log.error("Database inconsistency!");
            fillChild(root, path, 0, f);
        }
    }

    private void fillChild(BaseElement parent, String[] path, int pos, SoundFile soundFile) {
        if (pos == path.length) {
            return;
        }

        BaseElement element;
        if (pos == path.length - 1) {
            element = new SoundElement(soundFile);
            parent.addChild(element);
        } else {
            BaseElement catalogue = parent.getChildElement(path[pos]);
            if (catalogue == null) {
                element = new BaseElement();
                element.setName(path[pos]);
                fillChild(element, path, pos + 1, soundFile);
                parent.addChild(element);
            } else {
                fillChild(catalogue, path, pos + 1, soundFile);
            }
        }
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
