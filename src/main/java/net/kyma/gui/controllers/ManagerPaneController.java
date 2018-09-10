package net.kyma.gui.controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.DataQuery;
import net.kyma.dm.IndexingRoot;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.gui.NetworkPopup;
import net.kyma.gui.TableColumnFactory;
import net.kyma.gui.tree.*;
import net.kyma.player.PlaylistEvent;
import net.kyma.player.PlaylistRefreshEvent;
import org.apache.commons.lang3.StringUtils;
import pl.khuzzuk.messaging.Bus;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.kyma.EventType.*;

@Log4j2
@RequiredArgsConstructor
public class ManagerPaneController implements Initializable {
    @FXML
    private GridPane managerPane;
    @FXML
    @Getter
    private TableView<SoundFile> contentView;
    @FXML
    @Getter
    private TreeView<String> filesList;
    @FXML
    private TableView<SoundFile> playlist;
    @FXML
    @Getter
    private ListView<String> moodFilter;
    @FXML
    private ListView<String> genreFilter;
    @FXML
    private ListView<String> occasionFilter;

    private final Bus<EventType> bus;
    private TableColumnFactory columnFactory;
    private IntegerProperty highlighted;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        columnFactory = new TableColumnFactory(bus);
        highlighted = new SimpleIntegerProperty(-1);
        bus.subscribingFor(DATA_SET_DISTINCT_MOOD).onFXThread()
                .accept((Collection<String> values) -> setupFilter(moodFilter.getItems(), values))
                .subscribe();
        bus.subscribingFor(DATA_SET_DISTINCT_GENRE).onFXThread()
                .accept((Collection<String> values) -> setupFilter(genreFilter.getItems(), values))
                .subscribe();
        bus.subscribingFor(DATA_SET_DISTINCT_OCCASION).onFXThread()
                .accept((Collection<String> values) -> setupFilter(occasionFilter.getItems(), values))
                .subscribe();
        bus.subscribingFor(DATA_SET_DISTINCT_PEOPLE).onFXThread()
                .accept((Collection<String> values) -> addFilterToTreeView(SupportedField.ARTIST, values))
                .subscribe();

        bus.subscribingFor(DATA_REFRESH_PATHS).onFXThread().accept(this::fillPaths).subscribe();
        bus.subscribingFor(PLAYLIST_REFRESH).onFXThread().accept(this::refresh).subscribe();
        bus.subscribingFor(DATA_STORE_ITEM).onFXThread().accept(s -> contentView.refresh()).subscribe();
        bus.subscribingFor(DATA_STORE_LIST).onFXThread().accept(s -> contentView.refresh()).subscribe();
        bus.subscribingFor(DATA_QUERY_RESULT_FOR_CONTENT_VIEW).onFXThread().accept(this::fillContentView).subscribe();
        bus.subscribingFor(DATA_QUERY_RESULT_FOR_CONTENT_VIEW).onFXThread().accept(this::updateFilters).subscribe();

        filesList.setRoot(new RootElement("Content"));
        addNetworkView();

        initPlaylistView();

        bus.message(DATA_GET_PATHS).send();
    }

    private void initPlaylistView() {
        playlist.getColumns().clear();
        TableColumn<SoundFile, String> columnForPlaylist = columnFactory.getTitleColumnForPlaylist(highlighted);
        columnForPlaylist.setSortable(false);
        playlist.getColumns().add(columnForPlaylist);
        playlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void addNetworkView() {
        NetworkPopup networkPopup = new NetworkPopup(bus);
        networkPopup.init();
        NetworkElement networkElement = new NetworkElement(networkPopup);
        networkElement.setName("Import from youtube");
        RootElement root = (RootElement) filesList.getRoot();
        root.addChild(networkElement);
    }

    private synchronized void fillPaths(Map<IndexingRoot, Collection<String>> paths) {
        RootElement root = (RootElement) filesList.getRoot();
        for (Map.Entry<IndexingRoot, Collection<String>> entry : paths.entrySet()) {
            String rootName = entry.getKey().representation();
            RootElement newIndexingRoot = new RootElement(entry.getKey().representation());
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
    private void requestUpdateContentView() {
        DataQuery query = DataQuery.newQuery();

        BaseElement selectedItem = (BaseElement) filesList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (selectedItem instanceof NetworkElement) {
                ((NetworkElement) selectedItem).importFromNetwork();
                return;
            }
            selectedItem.applyTo(query);
        }

        applyFilterToQuery(moodFilter, query, SupportedField.MOOD);
        applyFilterToQuery(genreFilter, query, SupportedField.GENRE);
        applyFilterToQuery(occasionFilter, query, SupportedField.OCCASION);

        if (query.hasParameters()) {
            bus.message(DATA_QUERY).withContent(query).withResponse(DATA_QUERY_RESULT_FOR_CONTENT_VIEW).send();
        }
    }

    private void applyFilterToQuery(ListView<String> filter, DataQuery query, SupportedField field) {
        String selectedItem = filter.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.equals("...")) {
            query.and(field, selectedItem, false);
        }
    }

    private void fillContentView(Collection<SoundFile> soundFiles) {
        contentView.getItems().clear();
        contentView.getItems().addAll(soundFiles);
        contentView.refresh();
    }

    private void updateFilters(Collection<SoundFile> soundFiles) {
        setupFilter(moodFilter.getItems(), filterValuesFrom(soundFiles, SoundFile::getMood));
        setupFilter(genreFilter.getItems(), filterValuesFrom(soundFiles, SoundFile::getGenre));
        setupFilter(occasionFilter.getItems(), filterValuesFrom(soundFiles, SoundFile::getOccasion));
    }

    private Set<String> filterValuesFrom(Collection<SoundFile> soundFiles, Function<SoundFile, String> mapper) {
        return soundFiles.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toSet());
    }

    @FXML
    private void onKeyReleased(KeyEvent keyEvent) {
        BaseElement selected = (BaseElement) filesList.getSelectionModel().getSelectedItem();
        if (keyEvent.getCode() == KeyCode.INSERT && selected != null) {
            bus.message(DATA_INDEX_DIRECTORY).withContent(new File(selected.getFullPath())).send();
        }
    }
}
