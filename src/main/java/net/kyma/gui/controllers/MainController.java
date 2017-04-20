package net.kyma.gui.controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
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

@Singleton
@Log4j2
public class MainController implements Initializable {
    @FXML
    private TableView<SoundFile> contentView;
    @FXML
    private TreeView<String> filesList;
    @FXML
    private TableView<SoundFile> playlist;
    @FXML
    private GridPane playerPane;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private SoundFileConverter converter;
    @Inject
    private TableColumnFactory columnFactory;
    private static Set<String> fileExtensions;
    private IntegerProperty highlighted;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bus.<SoundFile>setReaction(messages.getProperty("playlist.add.sound"), playlist.getItems()::add);
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("playlist.add.list"), c -> playlist.getItems().addAll(c));
        bus.setGuiReaction(messages.getProperty("data.view.refresh"), this::fillTreeView);
        bus.setReaction(messages.getProperty("playlist.highlight"), this::highlight);
        fileExtensions = new HashSet<>();
        fileExtensions.add(".mp3");
        highlighted = new SimpleIntegerProperty(-1);

        initPlaylistView();
        initContentView();

        bus.sendCommunicate(messages.getProperty("data.index.getAll"), messages.getProperty("data.convert.from.doc.gui"));
    }

    private void initPlaylistView() {
        playlist.getColumns().clear();
        playlist.getColumns().add(columnFactory.getTitleColumn(highlighted));
    }

    @SuppressWarnings("unchecked")
    private void initContentView() {
        contentView.getColumns().clear();
        contentView.getColumns().addAll(columnFactory.getTitleColumn(), columnFactory.getRateColumn());
    }

    private void fillTreeView(Collection<SoundFile> sounds) {
        BaseElement root = new RootElement();
        List<SoundFile> soundFiles = sounds.stream().sorted().collect(Collectors.toList());
        for (SoundFile f : soundFiles) {
            String[] path = f.getIndexedPath().split("[\\\\/]+");
            if (path.length == 0) log.error("Database inconsistency!");
            fillChild(root, path, 0, f);
        }
        filesList.setRoot(root);
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
        Optional.ofNullable(getFile(new FileChooser.ExtensionFilter("Pliki dźwiękowe", "*.mp3")))
                .ifPresent(f -> bus.send(messages.getProperty("playlist.add.file"), messages.getProperty("playlist.add.sound"), f));
    }

    @FXML
    private void indexCatalogue() {
        Optional.ofNullable(getFile()).ifPresent(f -> bus.send(messages.getProperty("data.index.list"),
                getFilesFromDirectory(f).stream().map(current ->
                        converter.from(current, f.getPath().substring(0, f.getPath().length() - f.getName().length())))
                        .collect(Collectors.toList())));
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

    private Collection<File> getFilesFromDirectory(File file) {
        if (file.isFile()) {
            if (fileExtensions.contains(file.getName().substring(file.getName().length() - 4))) {
                return Collections.singletonList(file);
            } else {
                return Collections.emptyList();
            }
        }
        Optional<File[]> content = Optional.ofNullable(file.listFiles());
        return Arrays.stream(content.orElse(new File[]{})).flatMap(f -> getFilesFromDirectory(f).stream())
                .collect(Collectors.toList());
    }

    @FXML
    private void addToPlaylist(MouseEvent mouseEvent) {
        if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
        if (mouseEvent.getClickCount() != 2) return;
        //TODO extend TreeView so it can return of selected BaseElement or SoundElement instead of casting and instanceof
        bus.send(messages.getProperty("playlist.add.list"), contentView.getSelectionModel().getSelectedItems());
    }

    private void highlight(int pos) {
        highlighted.setValue(pos);
        playlist.refresh();
    }

    @FXML
    private void fillContentView() {
        BaseElement selectedItem = (BaseElement) filesList.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !(selectedItem instanceof SoundElement)) {
            contentView.getItems().clear();
            contentView.getItems().addAll(selectedItem.getChildElements().values()
                    .stream().filter(e -> e instanceof SoundElement)
                    .map(e -> (SoundElement) e)
                    .map(SoundElement::getSoundFile)
                    .collect(Collectors.toList()));
        }
    }
}
