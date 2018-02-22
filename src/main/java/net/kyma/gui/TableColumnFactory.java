package net.kyma.gui;

import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.DATA_UPDATE_REQUEST;
import static net.kyma.dm.SupportedField.TITLE;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import lombok.AllArgsConstructor;
import net.kyma.EventType;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.dm.TagUpdateRequest;
import org.apache.commons.lang3.StringUtils;
import pl.khuzzuk.messaging.Bus;

@AllArgsConstructor
public class TableColumnFactory {
    private Bus<EventType> bus;

    private static BiConsumer<SoundFile, TableCell<?, ?>> startFactory =
            (s, cell) -> Optional.ofNullable(s).map(Rating::getStarFor).ifPresent(cell::setGraphic);

    public TableColumn<SoundFile, String> getTitleColumn() {
        TableColumn<SoundFile, String> title = new TableColumn<>("Tytuł");
        title.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTitle()));
        title.setCellFactory(TextFieldTableCell.forTableColumn());
        title.setOnEditCommit(v ->
              bus.send(DATA_UPDATE_REQUEST, new TagUpdateRequest(TITLE, v.getNewValue())));

        title.setCellValueFactory(param -> {
            if (!StringUtils.isEmpty(param.getValue().getTitle())) {
                return new SimpleStringProperty(param.getValue().getTitle());
            }
            else {
                String name = param.getValue().getFileName();
                return new SimpleObjectProperty(name.substring(0, name.lastIndexOf(".")));
            }
        });
        return title;
    }

    public TableColumn<SoundFile, SoundFile> getRateColumn() {
        TableColumn<SoundFile, SoundFile> column = new TableColumn<>("Ocena");
        column.setComparator(Comparator.comparingInt(soundFile -> soundFile.getRate().getValue()));
        column.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue()));
        column.setCellFactory(param -> new TableCell<>()
        {
            @Override
            protected void updateItem(SoundFile item, boolean empty)
            {
                super.updateItem(item, empty);
                startFactory.accept(item, this);
                setOnMouseMoved(e -> Optional.ofNullable(item).ifPresent(i ->
                      setGraphic(Rating.getStarFor((int) (e.getX() * 10 / getWidth() + 1)))));
                setOnMouseExited(e -> startFactory.accept(item, this));
                setOnMouseClicked(e -> {
                    if (e.getButton().equals(MouseButton.PRIMARY))
                    {
                        Rating.setRate((int) (e.getX() * 10 / getWidth() + 1), item);
                        bus.send(DATA_STORE_ITEM, item);
                    }
                });
            }
        });
        column.setPrefWidth(100);
        column.setMaxWidth(100);
        return column;
    }

    public TableColumn<SoundFile, String> getStringColumn(SupportedField field)
    {
        TableColumn<SoundFile, String> column = new TableColumn<>(field.getName());
        column.setCellValueFactory(data -> new SimpleStringProperty(field.getGetter().apply(data.getValue())));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(value ->
              bus.send(DATA_UPDATE_REQUEST, new TagUpdateRequest(field, value.getNewValue())));
        return column;
    }

    public TableColumn<SoundFile, Integer> getCounterColumn() {
        TableColumn<SoundFile, Integer> column = new TableColumn<>("Odtworzono");
        column.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getCounter()));
        return column;
    }

    public TableColumn<SoundFile, String> getTitleColumnForPlaylist(IntegerProperty property) {
        TableColumn<SoundFile, String> column = getTitleColumn();
        column.setCellFactory(param -> new TableCell<SoundFile, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Label label = new Label(item);
                if (getIndex() == property.get()) {
                    getStyleClass().clear();
                    getStyleClass().add("currentlyPlayed");
                } else {
                    getStyleClass().clear();
                    getStyleClass().addAll("cell", "indexed-cell", "table-cell", "table-column");
                }
                setGraphic(label);
            }
        });
        column.setOnEditCommit(v -> bus.send(DATA_UPDATE_REQUEST,
              new TagUpdateRequest(TITLE, v.getNewValue())));
        return column;
    }
}
