package net.kyma.gui.controllers;

import static net.kyma.EventType.DATA_GET_PATHS;
import static net.kyma.EventType.DATA_INDEXING_FINISH;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
import static net.kyma.EventType.DATA_QUERY;
import static net.kyma.EventType.DATA_QUERY_RESULT_FOR_CONTENT_VIEW;
import static net.kyma.EventType.DATA_REFRESH_PATHS;
import static net.kyma.EventType.DATA_SET_DISTINCT_GENRE;
import static net.kyma.EventType.DATA_SET_DISTINCT_MOOD;
import static net.kyma.EventType.DATA_SET_DISTINCT_OCCASION;
import static net.kyma.EventType.DATA_SET_DISTINCT_PEOPLE;
import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.DATA_STORE_LIST;
import static net.kyma.EventType.PLAYLIST_REFRESH;
import static net.kyma.EventType.PLAYLIST_REMOVE_LIST;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.gui.TableColumnFactory;
import net.kyma.gui.tree.BaseElement;
import net.kyma.gui.tree.ContentElement;
import net.kyma.gui.tree.FilterRootElement;
import net.kyma.gui.tree.PathElementFactory;
import net.kyma.gui.tree.RootElement;
import net.kyma.player.PlaylistEvent;
import net.kyma.player.PlaylistRefreshEvent;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class ManagerPaneController implements Initializable, Loadable {
    @FXML
    private GridPane managerPane;
    @FXML
    private TableView<SoundFile> contentView;
    @FXML
    private TreeView<String> filesList;
    @FXML
    private TableView<SoundFile> playlist;
    @FXML
    private ListView<String> moodFilter;
    @FXML
    private ListView<String> genreFilter;
    @FXML
    private ListView<String> occasionFilter;

    private final Bus<EventType> bus;
    private TableColumnFactory columnFactory;
    private IntegerProperty highlighted;

    @Override
    public void load() {
        columnFactory = new TableColumnFactory(bus);
        highlighted = new SimpleIntegerProperty(-1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bus.subscribingFor(DATA_SET_DISTINCT_MOOD)
                .accept((Collection<String> values) -> setupFilter(moodFilter.getItems(), values))
                .subscribe();
        bus.subscribingFor(DATA_SET_DISTINCT_GENRE)
                .accept((Collection<String> values) -> setupFilter(genreFilter.getItems(), values))
                .subscribe();
        bus.subscribingFor(DATA_SET_DISTINCT_OCCASION)
                .accept((Collection<String> values) -> setupFilter(occasionFilter.getItems(), values))
                .subscribe();
        bus.subscribingFor(DATA_SET_DISTINCT_PEOPLE).onFXThread()
                .accept((Collection<String> values) -> addFilterToTreeView(SupportedField.ARTIST, values))
                .subscribe();

        bus.subscribingFor(DATA_REFRESH_PATHS).onFXThread().accept(this::fillPaths).subscribe();
        bus.subscribingFor(PLAYLIST_REFRESH).accept(this::refresh).subscribe();
        bus.subscribingFor(DATA_STORE_ITEM).accept(s -> contentView.refresh()).subscribe();
        bus.subscribingFor(DATA_STORE_LIST).accept(s -> contentView.refresh()).subscribe();
        bus.subscribingFor(DATA_QUERY_RESULT_FOR_CONTENT_VIEW).onFXThread()
                .<Collection<SoundFile>>accept(this::fillContentView)
                .subscribe();

        filesList.setRoot(new RootElement("Content"));

        initPlaylistView();

        bus.message(DATA_GET_PATHS).withResponse(DATA_REFRESH_PATHS).send();
    }

    private void initPlaylistView() {
        playlist.getColumns().clear();
        TableColumn<SoundFile, String> columnForPlaylist = columnFactory.getTitleColumnForPlaylist(highlighted);
        columnForPlaylist.setSortable(false);
        playlist.getColumns().add(columnForPlaylist);
        playlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private synchronized void fillPaths(Map<String, Collection<String>> paths) {
        RootElement root = (RootElement) filesList.getRoot();
        for (Map.Entry<String, Collection<String>> entry : paths.entrySet()) {
            String rootName = entry.getKey().substring(0, entry.getKey().length() - 1);
            RootElement newIndexingRoot = new RootElement(entry.getKey());
            newIndexingRoot.setName(rootName);

            for (String path : entry.getValue()) {
                String[] fractured = path.split("/");
                PathElementFactory.fillChild(newIndexingRoot, fractured, 0);
            }

            RootElement currentIndexingRoot = (RootElement) root.getChildElement(rootName);
            if (currentIndexingRoot != null) {
                currentIndexingRoot.update(newIndexingRoot);
            } else {
                root.addChild(newIndexingRoot);
            }
            bus.message(DATA_INDEXING_FINISH).send();
        }
    }

    private synchronized void addFilterToTreeView(SupportedField filterField, Collection<String> values) {
        RootElement root = (RootElement) filesList.getRoot();
        BaseElement filterElement = root.hasChild(filterField.getName())
                ? root.getChildElement(filterField.getName())
                : new FilterRootElement();
        filterElement.setName(filterField.getName());
        root.addChild(filterElement);

        for (String value : values) {
            ContentElement element = new ContentElement(bus, filterField);
            element.setName(value);
            filterElement.addChild(element);
        }
    }

    @FXML
    private void removeFromPlaylist(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
            List<SoundFile> selectedItems = playlist.getSelectionModel().getSelectedItems();
            List<SoundFile> playlistItems = playlist.getItems();
            List<PlaylistEvent> playlistEvents = new ArrayList<>(selectedItems.size());
            for (SoundFile soundFile : selectedItems) {
                playlistEvents.add(new PlaylistEvent(soundFile, playlistItems.indexOf(soundFile)));
            }
            bus.message(PLAYLIST_REMOVE_LIST).withContent(playlistEvents).send();
        }
    }

    private void refresh(PlaylistRefreshEvent refreshEvent) {
        playlist.getItems().clear();
        playlist.getItems().addAll(refreshEvent.getPlaylist());
        highlighted.setValue(refreshEvent.getPosition());
        playlist.refresh();
    }

    private void setupFilter(Collection<String> filter, Collection<String> values) {
        filter.clear();
        filter.add("...");
        filter.addAll(values);
        filter.remove("");
    }

    @FXML
    private void fillContentView() {
        BaseElement selectedItem = (BaseElement) filesList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            bus.message(DATA_QUERY).withContent(selectedItem.toQuery()).send();
        }
    }

    private void fillContentView(Collection<SoundFile> soundFiles) {
        contentView.getItems().clear();
        contentView.getItems().addAll(soundFiles);
        contentView.refresh();
    }

    @FXML
    private void onKeyReleased(KeyEvent keyEvent) {
        BaseElement selected = (BaseElement) filesList.getSelectionModel().getSelectedItem();
        if (keyEvent.getCode() == KeyCode.INSERT && selected != null) {
            bus.message(DATA_INDEX_DIRECTORY).withContent(new File(selected.getFullPath())).send();
        }
    }

    void resizeFor(SplitPane splitPane) {
        splitPane.heightProperty().addListener((obs, o, n) -> filesList.setMinHeight(n.doubleValue() - 150));
    }
}
