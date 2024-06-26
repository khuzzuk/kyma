package net.kyma.gui.manager;

import static net.kyma.EventType.DATA_GET_PATHS;
import static net.kyma.EventType.DATA_INDEXING_FINISH;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.DataQuery;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.gui.components.TableColumnFactory;
import net.kyma.gui.tree.BaseElement;
import net.kyma.gui.tree.ContentElement;
import net.kyma.gui.tree.FilterRootElement;
import net.kyma.gui.tree.RootElement;
import net.kyma.player.PlaylistEvent;
import net.kyma.player.PlaylistRefreshEvent;
import org.apache.commons.lang3.StringUtils;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class ManagerPaneController {
    @Getter
    @Setter
    private TableView<SoundFile> contentView;
    @Getter
    @Setter
    private TreeView<String> filesList;
    @Getter
    @Setter
    private TableView<SoundFile> playlist;
    @Getter
    @Setter
    private ListView<String> moodFilter;
    @Setter
    private ListView<String> genreFilter;
    @Setter
    private ListView<String> occasionFilter;

    private final Bus<EventType> bus;
    private TableColumnFactory columnFactory;
    private IntegerProperty highlighted;

    public void initialize() {
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

    private synchronized void fillPaths(Map<String, Collection<String>> paths) {
        RootElement root = (RootElement) filesList.getRoot();
        for (Map.Entry<String, Collection<String>> entry : paths.entrySet()) {
            String rootName = entry.getKey().substring(0, entry.getKey().length() - 1);
            RootElement newIndexingRoot = new RootElement(entry.getKey());
            newIndexingRoot.setName(rootName);

            for (String path : entry.getValue()) {
                String[] fractured = path.split("/");
                newIndexingRoot.addChildFor(fractured, 0);
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
            ContentElement element = new ContentElement(filterField);
            element.setName(value);
            filterElement.addChild(element);
        }
    }

    void removeFromPlaylist(KeyEvent keyEvent) {
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

    void requestUpdateContentView() {
        DataQuery query = DataQuery.newQuery();
        applyFilterToQuery(moodFilter, query, SupportedField.MOOD);
        applyFilterToQuery(genreFilter, query, SupportedField.GENRE);
        applyFilterToQuery(occasionFilter, query, SupportedField.OCCASION);

        BaseElement selectedItem = (BaseElement) filesList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.onClick(bus, query);
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

    void onKeyReleased(KeyEvent keyEvent) {
        BaseElement selected = (BaseElement) filesList.getSelectionModel().getSelectedItem();
        if (keyEvent.getCode() == KeyCode.INSERT && selected != null) {
            bus.message(DATA_INDEX_DIRECTORY).withContent(new File(selected.getFullPath())).send();
        }
    }
}
