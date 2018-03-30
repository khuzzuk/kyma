package net.kyma.gui.controllers;

import static net.kyma.EventType.DATA_CONVERT_FROM_DOC;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
import static net.kyma.EventType.DATA_INDEX_GET_ALL;
import static net.kyma.EventType.DATA_REFRESH;
import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.DATA_STORE_LIST;
import static net.kyma.EventType.PLAYLIST_ADD_LIST;
import static net.kyma.EventType.PLAYLIST_ADD_SOUND;
import static net.kyma.EventType.PLAYLIST_HIGHLIGHT;
import static net.kyma.EventType.PLAYLIST_REMOVE_LIST;
import static net.kyma.EventType.PLAYLIST_REMOVE_SOUND;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import net.kyma.gui.BaseElement;
import net.kyma.gui.RootElement;
import net.kyma.gui.SoundElement;
import net.kyma.gui.TableColumnFactory;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class ManagerPaneController implements Initializable {
    @FXML
    private GridPane managerPane;
    @FXML
    private TableView<SoundFile> contentView;
    @FXML
    private TreeView<String> filesList;
    @FXML
    private TableView<SoundFile> playlist;

    private final Bus<EventType> bus;
    private final TableColumnFactory columnFactory;
    private IntegerProperty highlighted;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Collection<SoundFile> playlistItems = playlist.getItems();
        bus.subscribingFor(PLAYLIST_ADD_SOUND).accept(playlistItems::add).subscribe();
        bus.subscribingFor(PLAYLIST_ADD_LIST).accept(playlistItems::addAll).subscribe();
        bus.subscribingFor(PLAYLIST_REMOVE_LIST).<Collection<SoundFile>>accept(playlistItems::removeAll).subscribe();
        bus.subscribingFor(PLAYLIST_REMOVE_SOUND).<Collection<SoundFile>>accept(soundFiles -> {
            playlistItems.removeAll(soundFiles);
            removeFromTreeView(soundFiles);
        }).subscribe();
        bus.subscribingFor(DATA_REFRESH).onFXThread().accept(this::fillTreeView).subscribe();
        bus.subscribingFor(PLAYLIST_HIGHLIGHT).accept(this::highlight).subscribe();
        bus.subscribingFor(DATA_STORE_ITEM).accept(s -> contentView.refresh()).subscribe();
        bus.subscribingFor(DATA_STORE_LIST).accept(s -> contentView.refresh()).subscribe();

        highlighted = new SimpleIntegerProperty(-1);

        initPlaylistView();

        bus.message(DATA_INDEX_GET_ALL).withResponse(DATA_CONVERT_FROM_DOC).send();
    }

    private void initPlaylistView() {
        playlist.getColumns().clear();
        playlist.getColumns().add(columnFactory.getTitleColumnForPlaylist(highlighted));
        playlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void fillTreeView(Collection<SoundFile> sounds) {
        RootElement root = new RootElement("Pliki");
        List<SoundFile> soundFiles = sounds.stream().sorted().collect(Collectors.toList());
        for (SoundFile f : soundFiles) {
            String[] path = f.getPathView();
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
            element.setParentElement(parent);
        } else {
            BaseElement catalogue = parent.getChildElement(path[pos]);
            if (catalogue == null) {
                element = pos == 0 ? new RootElement(soundFile.getIndexedPath()) : new BaseElement();
                element.setName(path[pos]);
                fillChild(element, path, pos + 1, soundFile);
                parent.addChild(element);
                element.setParentElement(parent);
            } else {
                fillChild(catalogue, path, pos + 1, soundFile);
            }
        }
    }

    private void removeFromTreeView(Collection<SoundFile> soundFiles)
    {
        soundFiles.stream()
              .map(SoundFile::getPathView)
              .forEach(path -> {
                  BaseElement element = (BaseElement) filesList.getRoot();
                  for (String name : path) {
                      element = element.getChildElement(name);
                      if (element == null) {
                          log.error("Cannot find element in tree: {} in {}", name, path);
                          return;
                      }
                  }
                  element.detachFromParent();
              });
    }

    @FXML
    private void removeFromPlaylist(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
            bus.message(PLAYLIST_REMOVE_LIST).withContent(playlist.getSelectionModel().getSelectedItems()).send();
        }
    }

    private void highlight(int pos) {
        highlighted.setValue(pos);
        playlist.refresh();
    }

    @FXML
    private void fillContentView() {
        BaseElement selectedItem = (BaseElement) filesList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            contentView.getItems().clear();
            contentView.refresh();
            contentView.getItems().addAll(selectedItem.getChildElements().values()
                    .stream().filter(e -> e instanceof SoundElement)
                    .map(e -> (SoundElement) e)
                    .map(SoundElement::getSoundFile)
                    .collect(Collectors.toList()));
        }
    }
    
    @FXML
    private void onKeyReleased(KeyEvent keyEvent)
    {
        List<BaseElement> selected = filesList.getSelectionModel().getSelectedItems()
              .stream().map(BaseElement.class::cast)
              .collect(Collectors.toList());
        switch (keyEvent.getCode())
        {
            case INSERT:
                selected.stream()
                      .map(BaseElement::getPath)
                      .map(File::new)
                      .forEach(path -> bus.message(DATA_INDEX_DIRECTORY).withContent(path).send());
        }
    }

    void resizeFor(SplitPane splitPane) {
        splitPane.heightProperty().addListener((obs, o, n) -> filesList.setMinHeight(n.doubleValue() - 150));
    }
}
