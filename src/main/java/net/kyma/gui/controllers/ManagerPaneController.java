package net.kyma.gui.controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import net.kyma.gui.BaseElement;
import net.kyma.gui.RootElement;
import net.kyma.gui.SoundElement;
import net.kyma.gui.TableColumnFactory;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Singleton
public class ManagerPaneController implements Initializable {
    @FXML
    private TableView<SoundFile> contentView;
    @FXML
    private TreeView<String> filesList;
    @FXML
    private TableView<SoundFile> playlist;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private TableColumnFactory columnFactory;
    private IntegerProperty highlighted;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bus.<SoundFile>setReaction(messages.getProperty("playlist.add.sound"), playlist.getItems()::add);
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("playlist.add.list"),
                c -> playlist.getItems().addAll(c));
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("playlist.remove.list"),
                c -> playlist.getItems().removeAll(c));
        bus.setGuiReaction(messages.getProperty("data.view.refresh"), this::fillTreeView);
        bus.setReaction(messages.getProperty("playlist.highlight"), this::highlight);

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
        contentView.getColumns().addAll(columnFactory.getTitleColumn(),
                columnFactory.getRateColumn(),
                columnFactory.getYearColumn(),
                columnFactory.getAlbumColumn());
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
    private void addToPlaylist(MouseEvent mouseEvent) {
        if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
        if (mouseEvent.getClickCount() != 2) return;
        //TODO extend TreeView so it can return of selected BaseElement or SoundElement instead of casting and instanceof
        bus.send(messages.getProperty("playlist.add.list"), contentView.getSelectionModel().getSelectedItems());
    }

    @FXML
    private void removeFromPlaylist(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
            bus.send(messages.getProperty("playlist.remove.list"), playlist.getSelectionModel().getSelectedItems());
        }
    }

    private void highlight(int pos) {
        highlighted.setValue(pos);
        playlist.refresh();
    }

    @FXML
    private void fillContentView() {
        initContentView();
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
