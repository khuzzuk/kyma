package net.kyma.gui.controllers;

import static net.kyma.EventType.DATA_REMOVE_ITEM;
import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.DATA_UPDATE_REQUEST;
import static net.kyma.EventType.PLAYLIST_ADD_LIST;
import static net.kyma.EventType.PLAYLIST_NEXT;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
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
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class ContentView implements Initializable {
    @FXML
    private TableView<SoundFile> contentView;
    private Bus<EventType> bus;
    private TableColumnFactory columnFactory;
    private SoundFileEditor editor;
    private SoundFileBulkEditor bulkEditor;
    private Collection<SoundFile> selected;

    public ContentView(Bus<EventType> bus, TableColumnFactory columnFactory)
    {
        this.bus = bus;
        this.columnFactory = columnFactory;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initContentView();

        editor = new SoundFileEditor();
        editor.init();
        bulkEditor = new SoundFileBulkEditor();
        bulkEditor.init();

        contentView.setEditable(true);
        bus.setReaction(DATA_UPDATE_REQUEST, this::update);
        bus.setReaction(PLAYLIST_NEXT, contentView::refresh);
    }

    @SuppressWarnings("unchecked")
    private void initContentView() {
        contentView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        contentView.getColumns().clear();
        contentView.getColumns().addAll(
              columnFactory.getStringColumn(SupportedField.TITLE),
              columnFactory.getRateColumn(),
              columnFactory.getStringColumn(SupportedField.YEAR),
              columnFactory.getStringColumn(SupportedField.ALBUM),
              columnFactory.getStringColumn(SupportedField.ARTIST),
              columnFactory.getStringColumn(SupportedField.MOOD),
              columnFactory.getStringColumn(SupportedField.TEMPO),
              columnFactory.getStringColumn(SupportedField.OCCASION),
              columnFactory.getCounterColumn());
        selected = contentView.getSelectionModel().getSelectedItems();
    }

    private void update(TagUpdateRequest updateRequest)
    {
        bus.send(DATA_STORE_ITEM, updateRequest.update(getSelected()));
    }

    private SoundFile getSelected() {
        return contentView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void addToPlaylist(MouseEvent mouseEvent) {
        //TODO extend TreeView so it can return of selected BaseElement or SoundElement instead of casting and instanceof
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            bus.send(PLAYLIST_ADD_LIST, selected);
        }
        if (mouseEvent.getButton().equals(MouseButton.MIDDLE)) {
            editor.showEditor(getSelected());
        }
    }

    @FXML
    private void onKeyReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            Collection<SoundFile> allSelected = selected;
            if (allSelected.size() == 1) {
                editor.showEditor(getSelected());
            } else {
                bulkEditor.showEditor(allSelected);
            }
        } else if (keyEvent.getCode().equals(KeyCode.BACK_SPACE) || keyEvent.getCode().equals(KeyCode.DELETE)) {
            Collection<SoundFile> selected = new ArrayList<>(this.selected);
            bus.send(DATA_REMOVE_ITEM, selected);
            contentView.getItems().removeAll(selected);
            contentView.refresh();
        }
    }
}
