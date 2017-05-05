package net.kyma.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import net.kyma.gui.TableColumnFactory;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentView.setEditable(true);
        initContentView();
    }

    @SuppressWarnings("unchecked")
    private void initContentView() {
        contentView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        contentView.getColumns().clear();
        contentView.getColumns().addAll(columnFactory.getTitleColumn(),
                columnFactory.getRateColumn(),
                columnFactory.getYearColumn(),
                columnFactory.getAlbumColumn());

        bus.setReaction(messages.getProperty("data.edit.title.commit"), this::updateTitleSelected);
    }

    private void updateTitleSelected(String title) {
        SoundFile selectedItem = contentView.getSelectionModel().getSelectedItem();
        selectedItem.setTitle(title);
        bus.send(messages.getProperty("data.index.item"), selectedItem);
        bus.send(messages.getProperty("data.store.item"), selectedItem);
    }

    @FXML
    private void addToPlaylist(MouseEvent mouseEvent) {
        if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
        if (mouseEvent.getClickCount() != 2) return;
        //TODO extend TreeView so it can return of selected BaseElement or SoundElement instead of casting and instanceof
        bus.send(messages.getProperty("playlist.add.list"), contentView.getSelectionModel().getSelectedItems());
    }
}
