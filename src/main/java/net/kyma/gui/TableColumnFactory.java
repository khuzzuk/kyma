package net.kyma.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;
import org.apache.commons.lang3.StringUtils;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
public class TableColumnFactory {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;

    private static BiConsumer<SoundFile, TableCell<?, ?>> graphicSetter =
            (s, cell) -> Optional.ofNullable(s).map(Rating::getStarFor).ifPresent(cell::setGraphic);

    public TableColumn<SoundFile, String> getTitleColumn() {
        TableColumn<SoundFile, String> title = new TableColumn<>("Tytuł");
        title.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTitle()));
        title.setCellFactory(TextFieldTableCell.forTableColumn());
        title.setOnEditCommit(v -> bus.send(messages.getProperty("data.edit.title.commit"), v.getNewValue()));
        return title;
    }

    public TableColumn<SoundFile, SoundFile> getRateColumn() {
        TableColumn<SoundFile, SoundFile> column = new TableColumn<>("Ocena");
        column.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue()));
        column.setCellFactory(param -> new TableCell<SoundFile, SoundFile>() {
            @Override
            protected void updateItem(SoundFile item, boolean empty) {
                super.updateItem(item, empty);
                graphicSetter.accept(item, this);
                setOnMouseMoved(e -> Optional.ofNullable(item).ifPresent(i ->
                        setGraphic(Rating.getStarFor((int) (e.getX() * 10 / getWidth() + 1)))));
                setOnMouseExited(e -> graphicSetter.accept(item, this));
                setOnMouseClicked(e -> {
                    if (e.getButton().equals(MouseButton.PRIMARY)) {
                        Rating.setRate((int) (e.getX() * 10 / getWidth() + 1), item);
                        bus.send(messages.getProperty("data.store.item"), item);
                    }
                });
            }
        });
        column.setPrefWidth(100);
        column.setMaxWidth(100);
        return column;
    }

    public TableColumn<SoundFile, String> getYearColumn() {
        TableColumn<SoundFile, String> column = new TableColumn<>("rok");
        column.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getDate()));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(v -> bus.send(messages.getProperty("data.edit.year.commit"), v.getNewValue()));
        return column;
    }

    public TableColumn<SoundFile, String> getAlbumColumn() {
        TableColumn<SoundFile, String> album = new TableColumn<>("album");
        album.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getAlbum()));
        return album;
    }

    public TableColumn<SoundFile, String> getTitleColumn(IntegerProperty property) {
        TableColumn<SoundFile, String> column = getTitleColumn();
        column.setCellFactory(param -> new TableCell<SoundFile, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Label label = new Label(item);
                if (getIndex() == property.get()) {
                    getStyleClass().add("currentlyPlayed");
                } else {
                    getStyleClass().remove("currentlyPlayed");
                }
                setGraphic(label);
            }
        });
        column.setOnEditCommit(v -> bus.send(messages.getProperty("data.edit.title.commit.playlist"), v));
        return column;
    }
}
