package net.kyma.gui.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import net.kyma.gui.SoundFileBulkEditor;
import net.kyma.gui.SoundFileEditor;
import net.kyma.gui.TableColumnFactory;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@Singleton
public class ContentView implements Initializable {
    public TableView<SoundFile> contentView;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private TableColumnFactory columnFactory;
    @Inject
    private SoundFileEditor editor;
    @Inject
    private SoundFileBulkEditor bulkEditor;
    private Collection<SoundFile> selected;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initContentView();

        contentView.setEditable(true);
        bus.<String>setReaction(messages.getProperty("data.edit.title.commit"), v -> update(v, (n, s) -> s.setTitle(n)));
        bus.<String>setReaction(messages.getProperty("data.edit.year.commit"), v -> update(v, (n, s) -> s.setDate(n)));
        bus.setReaction(messages.getProperty("playlist.next"), contentView::refresh);
    }

    @SuppressWarnings("unchecked")
    private void initContentView() {
        contentView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        contentView.getColumns().clear();
        contentView.getColumns().addAll(columnFactory.getTitleColumn(),
                columnFactory.getRateColumn(),
                columnFactory.getYearColumn(),
                columnFactory.getAlbumColumn(),
                columnFactory.getAlbumArtistColumn(),
                columnFactory.getAlbumArtistsColumn(),
                columnFactory.getArtistColumn(),
                columnFactory.getArtistsColumn(),
                columnFactory.getCounterColumn());
        selected = contentView.getSelectionModel().getSelectedItems();
    }

    private <T> void update(T value, BiConsumer<T, SoundFile> updater) {
        SoundFile selected = getSelected();
        updater.accept(value, selected);
        bus.send(messages.getProperty("data.store.item"), selected);
    }

    private SoundFile getSelected() {
        return contentView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void addToPlaylist(MouseEvent mouseEvent) {
        //TODO extend TreeView so it can return of selected BaseElement or SoundElement instead of casting and instanceof
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            bus.send(messages.getProperty("playlist.add.list"), selected);
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
            bus.send(messages.getProperty("data.remove.item"), selected);
            contentView.getItems().removeAll(selected);
            contentView.refresh();
        }
    }
}
