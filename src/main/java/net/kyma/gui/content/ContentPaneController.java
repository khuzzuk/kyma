package net.kyma.gui.content;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import net.kyma.dm.StringTagUpdateRequest;
import net.kyma.dm.SupportedField;
import net.kyma.dm.TagUpdateRequest;
import net.kyma.gui.components.editor.SoundFileBulkEditor;
import net.kyma.gui.components.editor.SoundFileEditor;
import net.kyma.gui.components.TableColumnFactory;
import net.kyma.properties.UIProperties;
import org.apache.commons.lang3.StringUtils;
import pl.khuzzuk.messaging.Bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static net.kyma.EventType.DATA_INDEX_GET_DISTINCT;
import static net.kyma.EventType.DATA_REMOVE_ITEM;
import static net.kyma.EventType.DATA_SET_DISTINCT_CUSTOM1;
import static net.kyma.EventType.DATA_SET_DISTINCT_CUSTOM2;
import static net.kyma.EventType.DATA_SET_DISTINCT_CUSTOM3;
import static net.kyma.EventType.DATA_SET_DISTINCT_CUSTOM4;
import static net.kyma.EventType.DATA_SET_DISTINCT_CUSTOM5;
import static net.kyma.EventType.DATA_SET_DISTINCT_GENRE;
import static net.kyma.EventType.DATA_SET_DISTINCT_INSTRUMENT;
import static net.kyma.EventType.DATA_SET_DISTINCT_MOOD;
import static net.kyma.EventType.DATA_SET_DISTINCT_OCCASION;
import static net.kyma.EventType.DATA_SET_DISTINCT_PEOPLE;
import static net.kyma.EventType.DATA_SET_DISTINCT_TEMPO;
import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.DATA_UPDATE_REQUEST;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_CHANGED;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_GET;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_SET;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_STORE;
import static net.kyma.EventType.PLAYLIST_ADD_LIST;
import static net.kyma.EventType.PLAYLIST_NEXT;
import static net.kyma.EventType.PLAYLIST_REMOVE_SOUND;
import static net.kyma.dm.SupportedField.ARTIST;
import static net.kyma.dm.SupportedField.COMPOSER;
import static net.kyma.dm.SupportedField.CONDUCTOR;
import static net.kyma.dm.SupportedField.CUSTOM1;
import static net.kyma.dm.SupportedField.CUSTOM2;
import static net.kyma.dm.SupportedField.CUSTOM3;
import static net.kyma.dm.SupportedField.CUSTOM4;
import static net.kyma.dm.SupportedField.CUSTOM5;
import static net.kyma.dm.SupportedField.GENRE;
import static net.kyma.dm.SupportedField.INSTRUMENT;
import static net.kyma.dm.SupportedField.MOOD;
import static net.kyma.dm.SupportedField.OCCASION;
import static net.kyma.dm.SupportedField.SUPPORTED_TAG;
import static net.kyma.dm.SupportedField.TEMPO;
import static net.kyma.dm.SupportedField.TITLE;

@Log4j2
@RequiredArgsConstructor
public class ContentPaneController implements Loadable {
    @Setter(AccessLevel.PACKAGE)
    private TableView<SoundFile> mainContentView;
    private ContextMenu columnContextMenu;
    private ContextMenu contentContextMenu;
    private final Bus<EventType> bus;
    private TableColumnFactory columnFactory;
    private SoundFileEditor editor;
    private SoundFileBulkEditor bulkEditor;
    private Collection<SoundFile> selected;
    private Map<SupportedField, Collection<String>> suggestions;

    @Override
    public void load() {
        columnFactory = new TableColumnFactory(bus);
        suggestions = new EnumMap<>(SupportedField.class);
        editor = new SoundFileEditor(bus);
        bulkEditor = new SoundFileBulkEditor(bus);
    }

    public void initialize() {
        initSuggestions();
        editor.init(suggestions);
        bulkEditor.init(suggestions);

        initContentView();
    }

    @SuppressWarnings("unchecked")
    private void initContentView() {
        createContextMenu(Collections.emptyList());

        mainContentView.setEditable(true);
        mainContentView.getSelectionModel().cellSelectionEnabledProperty().setValue(true);
        mainContentView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mainContentView.getColumns().clear();
        selected = mainContentView.getSelectionModel().getSelectedItems();

        bus.subscribingFor(GUI_CONTENTVIEW_SETTINGS_CHANGED).then(this::sendContentViewSettings).subscribe();
        bus.subscribingFor(DATA_UPDATE_REQUEST).accept((TagUpdateRequest request) -> update(request, getSelected())).subscribe();
        bus.subscribingFor(PLAYLIST_NEXT).then(mainContentView::refresh).subscribe();
        bus.subscribingFor(GUI_CONTENTVIEW_SETTINGS_SET).onFXThread().accept(this::setupColumns).subscribe();
        bus.message(GUI_CONTENTVIEW_SETTINGS_GET).withResponse(GUI_CONTENTVIEW_SETTINGS_SET).send();

    }

    private void initSuggestions() {
        suggestions.put(MOOD, new TreeSet<>());
        suggestions.put(TEMPO, new TreeSet<>());
        suggestions.put(OCCASION, new TreeSet<>());
        suggestions.put(GENRE, new TreeSet<>());
        suggestions.put(INSTRUMENT, new TreeSet<>());
        suggestions.put(CUSTOM1, new TreeSet<>());
        suggestions.put(CUSTOM2, new TreeSet<>());
        suggestions.put(CUSTOM3, new TreeSet<>());
        suggestions.put(CUSTOM4, new TreeSet<>());
        suggestions.put(CUSTOM5, new TreeSet<>());

        Map.of(
                MOOD, DATA_SET_DISTINCT_MOOD,
                TEMPO, DATA_SET_DISTINCT_TEMPO,
                OCCASION, DATA_SET_DISTINCT_OCCASION,
                GENRE, DATA_SET_DISTINCT_GENRE,
                INSTRUMENT, DATA_SET_DISTINCT_INSTRUMENT,
                CUSTOM1, DATA_SET_DISTINCT_CUSTOM1,
                CUSTOM2, DATA_SET_DISTINCT_CUSTOM2,
                CUSTOM3, DATA_SET_DISTINCT_CUSTOM3,
                CUSTOM4, DATA_SET_DISTINCT_CUSTOM4,
                CUSTOM5, DATA_SET_DISTINCT_CUSTOM5
        ).forEach((field, eventType) -> {
            bus.subscribingFor(eventType).accept(suggestions.get(field)::addAll).subscribe();
            bus.message(DATA_INDEX_GET_DISTINCT).withResponse(eventType).withContent(field).send();
        });

        Set<String> peopleValues = new TreeSet<>();
        bus.subscribingFor(DATA_SET_DISTINCT_PEOPLE).accept(peopleValues::addAll).subscribe();
        bus.message(DATA_INDEX_GET_DISTINCT).withResponse(DATA_SET_DISTINCT_PEOPLE).withContent(ARTIST).send();
        bus.message(DATA_INDEX_GET_DISTINCT).withResponse(DATA_SET_DISTINCT_PEOPLE).withContent(COMPOSER).send();
        bus.message(DATA_INDEX_GET_DISTINCT).withResponse(DATA_SET_DISTINCT_PEOPLE).withContent(CONDUCTOR).send();
        suggestions.put(ARTIST, peopleValues);
        suggestions.put(COMPOSER, peopleValues);
        suggestions.put(CONDUCTOR, peopleValues);
    }

    private void setupColumns(Collection<UIProperties.ColumnDefinition> columnDefinitions) {
        createContextMenu(columnDefinitions.stream()
                .map(UIProperties.ColumnDefinition::getField)
                .collect(Collectors.toList()));

        ObservableList<TableColumn<SoundFile, ?>> columns = mainContentView.getColumns();
        columnDefinitions.stream()
                .map(columnDefinition -> columnFactory.getColumnFor(
                        columnDefinition.getField(),
                        columnDefinition.getSize(),
                        suggestions.get(columnDefinition.getField())))
                .forEach(columns::add);
    }

    private void createContextMenu(Collection<SupportedField> enabled) {
        columnContextMenu = new ContextMenu();
        for (SupportedField field : SUPPORTED_TAG) {
            CheckMenuItem menuItem = new CheckMenuItem(field.getName());
            menuItem.setSelected(enabled.contains(field));
            menuItem.setOnAction(onChangeColumnsAction(field, menuItem));
            columnContextMenu.getItems().add(menuItem);
        }

        contentContextMenu = new ContextMenu();
        MenuItem generateTitlesMenuItem = new MenuItem("Generate titles");
        generateTitlesMenuItem.setOnAction(this::onGenerateTitleAction);
        contentContextMenu.getItems().add(generateTitlesMenuItem);
    }

    private EventHandler<ActionEvent> onChangeColumnsAction(SupportedField field, CheckMenuItem menuItem) {
        return event -> {
            if (!menuItem.isSelected()) {
                removeColumnWithField(field);
            } else {
                mainContentView.getColumns().add(columnFactory.getColumnFor(field, 100, suggestions.get(field)));
                sendContentViewSettings();
            }
        };
    }

    private void onGenerateTitleAction(ActionEvent ignored) {
        contentContextMenu.hide();
        selected.stream()
                .filter(soundFile -> StringUtils.isBlank(soundFile.getTitle()))
                .forEach(soundFile -> update(
                        new StringTagUpdateRequest(TITLE, soundFile.getFileName().substring(0, soundFile.getFileName().lastIndexOf('.'))),
                        soundFile));

    }

    private void removeColumnWithField(SupportedField field) {
        mainContentView.getColumns().remove(
                mainContentView.getColumns().stream()
                        .filter(column -> column.getText().equals(field.getName()))
                        .findAny().orElse(null));
    }

    private void update(TagUpdateRequest updateRequest, SoundFile soundFile) {
        bus.message(DATA_STORE_ITEM).withContent(updateRequest.update(soundFile)).send();
    }

    private SoundFile getSelected() {
        return mainContentView.getSelectionModel().getSelectedItem();
    }

    void onMouseEvent(MouseEvent mouseEvent) {
        columnContextMenu.hide();
        MouseButton mouseButton = mouseEvent.getButton();

        switch (mouseButton) {
            case PRIMARY:
                if (mouseEvent.getClickCount() == 2) {
                    bus.message(PLAYLIST_ADD_LIST).withContent(selected).send();
                }
                break;

            case MIDDLE:
                editor.showEditor(getSelected());
                break;

            case SECONDARY:
                EventTarget target = mouseEvent.getTarget();
                if (target instanceof TableColumnHeader) {
                    columnContextMenu.show((TableColumnHeader) target, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                } else {
                    contentContextMenu.show(mainContentView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
                break;
            default:
                break;
        }
    }

    void onKeyReleased(KeyEvent keyEvent) {

        KeyCode keyCode = keyEvent.getCode();
        switch (keyCode) {
            case ENTER:
                if (keyEvent.isControlDown()) {
                    if (selected.size() == 1) {
                        editor.showEditor(getSelected());
                    } else if (selected.size() > 1) {
                        bulkEditor.showEditor(selected);
                    }
                } else if (keyEvent.isAltDown()) {
                    bus.message(PLAYLIST_ADD_LIST).withContent(selected).send();
                }
                break;

            case TAB:
            TablePosition<SoundFile, ?> editingCell = mainContentView.getEditingCell();
                if (editingCell != null) {
                    editingCell.getTableColumn().getOnEditCommit().handle(null);
                    int columnIndex = editingCell.getColumn();
                    int maxColumn = mainContentView.getColumns().size() - 1;
                    int nextColumn = columnIndex == maxColumn ? 0 : columnIndex + 1;
                    mainContentView.getSelectionModel().select(editingCell.getRow(), mainContentView.getColumns().get(nextColumn));
                    keyEvent.consume();
                }
                break;

            case BACK_SPACE:
            case DELETE:
                if (keyEvent.isControlDown()) {
                    Collection<SoundFile> selectedCopy = new ArrayList<>(this.selected);
                    bus.message(DATA_REMOVE_ITEM).withContent(selectedCopy).send();
                    bus.message(PLAYLIST_REMOVE_SOUND).withContent(selectedCopy).send();
                    mainContentView.getItems().removeAll(selectedCopy);
                    mainContentView.refresh();
                }
                break;
            default:
                break;
        }
    }

    private void sendContentViewSettings() {
        List<UIProperties.ColumnDefinition> definitions = mainContentView.getColumns().stream()
                .map(column -> new UIProperties.ColumnDefinition(
                        SupportedField.getByName(column.getText()),
                        column.getWidth()))
                .collect(Collectors.toCollection(ArrayList::new));
        bus.message(GUI_CONTENTVIEW_SETTINGS_STORE).withContent(definitions).send();
    }
}
