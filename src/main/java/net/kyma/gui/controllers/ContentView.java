package net.kyma.gui.controllers;

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
import static net.kyma.EventType.PLAYLIST_REMOVE_LIST;
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
import static net.kyma.dm.SupportedField.TEMPO;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.dm.TagUpdateRequest;
import net.kyma.gui.SoundFileBulkEditor;
import net.kyma.gui.SoundFileEditor;
import net.kyma.gui.TableColumnFactory;
import net.kyma.properties.UIProperties;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class ContentView implements Initializable
{
   @FXML
   private TableView<SoundFile> contentView;
   private ContextMenu contentViewContextMenu;
   private Bus<EventType> bus;
   private TableColumnFactory columnFactory;
   private SoundFileEditor editor;
   private SoundFileBulkEditor bulkEditor;
   private Collection<SoundFile> selected;
   private Map<SupportedField, Collection<String>> suggestions;

   public ContentView(Bus<EventType> bus, TableColumnFactory columnFactory)
   {
      this.bus = bus;
      this.columnFactory = columnFactory;
   }

   @Override
   public void initialize(URL location, ResourceBundle resources)
   {
      initContentView();

      editor = new SoundFileEditor(bus);
      editor.init(suggestions);
      bulkEditor = new SoundFileBulkEditor(bus);
      bulkEditor.init(suggestions);

      contentView.setEditable(true);
      bus.setReaction(GUI_CONTENTVIEW_SETTINGS_CHANGED, this::sendContentViewSettings);
      bus.setReaction(DATA_UPDATE_REQUEST, this::update);
      bus.setReaction(PLAYLIST_NEXT, contentView::refresh);
      bus.setFXReaction(GUI_CONTENTVIEW_SETTINGS_SET, this::setupColumns);
      bus.sendMessage(GUI_CONTENTVIEW_SETTINGS_GET, GUI_CONTENTVIEW_SETTINGS_SET);
   }

   @SuppressWarnings("unchecked")
   private void initContentView()
   {
      initSuggestions();
      createContextMenu(Collections.emptyList());

      contentView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      contentView.getColumns().clear();
      Node header = contentView.lookup(".column-header");
      //header.setOnContextMenuRequested(event -> contentViewContextMenu.show(header, event.getScreenX(), event.getScreenY()));
      selected = contentView.getSelectionModel().getSelectedItems();
   }

   private void initSuggestions()
   {
      suggestions = new HashMap<>();
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
               bus.setReaction(eventType, suggestions.get(field)::addAll);
               bus.send(DATA_INDEX_GET_DISTINCT, eventType, field);
            });

      Set<String> peopleValues = new TreeSet<>();
      bus.setReaction(DATA_SET_DISTINCT_PEOPLE, peopleValues::addAll);
      bus.send(DATA_INDEX_GET_DISTINCT, DATA_SET_DISTINCT_PEOPLE, ARTIST);
      bus.send(DATA_INDEX_GET_DISTINCT, DATA_SET_DISTINCT_PEOPLE, COMPOSER);
      bus.send(DATA_INDEX_GET_DISTINCT, DATA_SET_DISTINCT_PEOPLE, CONDUCTOR);
      suggestions.put(ARTIST, peopleValues);
      suggestions.put(COMPOSER, peopleValues);
      suggestions.put(CONDUCTOR, peopleValues);
   }

   private void setupColumns(Collection<UIProperties.ColumnDefinition> columnDefinitions)
   {
      createContextMenu(columnDefinitions.stream()
            .map(UIProperties.ColumnDefinition::getField)
            .collect(Collectors.toList()));

      ObservableList<TableColumn<SoundFile, ?>> columns = contentView.getColumns();
      columnDefinitions.stream()
            .map(columnDefinition -> columnFactory.getColumnFor(
                  columnDefinition.getField(),
                  columnDefinition.getSize(),
                  suggestions.get(columnDefinition.getField())))
            .forEach(columns::add);
   }

   private void createContextMenu(Collection<SupportedField> enabled)
   {
      contentViewContextMenu = new ContextMenu();
      for (SupportedField field : SupportedField.SUPPORTED_TAG)
      {
         CheckMenuItem menuItem = new CheckMenuItem(field.getName());
         menuItem.setSelected(enabled.contains(field));
         menuItem.setOnAction(onContextMenuAction(field, menuItem));
         contentViewContextMenu.getItems().add(menuItem);
      }
   }

   private EventHandler<ActionEvent> onContextMenuAction(SupportedField field, CheckMenuItem menuItem)
   {
      return event -> {
         if (!menuItem.isSelected())
         {
            removeColumnWithField(field);
         }
         else
         {
            contentView.getColumns().add(columnFactory.getColumnFor(field, 100, suggestions.get(field)));
            sendContentViewSettings();
         }
      };
   }

   private void removeColumnWithField(SupportedField field)
   {
      contentView.getColumns().remove(
            contentView.getColumns().stream()
                  .filter(column -> column.getText().equals(field.getName()))
                  .findAny().orElse(null));
   }

   private void update(TagUpdateRequest updateRequest)
   {
      bus.send(DATA_STORE_ITEM, updateRequest.update(getSelected()));
   }

   private SoundFile getSelected()
   {
      return contentView.getSelectionModel().getSelectedItem();
   }

   @FXML
   private void addToPlaylist(MouseEvent mouseEvent)
   {
      contentViewContextMenu.hide();
      MouseButton mouseButton = mouseEvent.getButton();
      if (mouseButton.equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2)
      {
         bus.send(PLAYLIST_ADD_LIST, selected);
      }
      if (mouseButton.equals(MouseButton.MIDDLE))
      {
         editor.showEditor(getSelected());
      }
      if (mouseButton.equals(MouseButton.SECONDARY))
      {
         EventTarget target = mouseEvent.getTarget();
         if (target instanceof TableColumnHeader)
         {
            contentViewContextMenu.show((TableColumnHeader) target, mouseEvent.getScreenX(), mouseEvent.getScreenY());
         }
      }
   }

   @FXML
   private void onKeyReleased(KeyEvent keyEvent)
   {
      if (keyEvent.getCode().equals(KeyCode.ENTER))
      {
         Collection<SoundFile> allSelected = selected;
         if (keyEvent.isControlDown())
         {
            if (allSelected.size() == 1)
            {
               editor.showEditor(getSelected());
            }
            else
            {
               bulkEditor.showEditor(allSelected);
            }
         }
         else if (keyEvent.isAltDown())
         {
            bus.send(PLAYLIST_ADD_LIST, selected);
         }
      }
      else if ((keyEvent.getCode().equals(KeyCode.BACK_SPACE) || keyEvent.getCode().equals(KeyCode.DELETE))
            && keyEvent.isControlDown())
      {
         Collection<SoundFile> selected = new ArrayList<>(this.selected);
         bus.send(DATA_REMOVE_ITEM, selected);
         bus.send(PLAYLIST_REMOVE_LIST, selected);
         contentView.getItems().removeAll(selected);
         contentView.refresh();
      }
   }

   private void sendContentViewSettings()
   {
      List<UIProperties.ColumnDefinition> definitions = contentView.getColumns().stream()
            .map(column -> new UIProperties.ColumnDefinition(
                  SupportedField.getByName(column.getText()),
                  column.getWidth()))
            .collect(Collectors.toCollection(ArrayList::new));
      bus.send(GUI_CONTENTVIEW_SETTINGS_STORE, definitions);
   }
}
