package net.kyma.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;

import javax.inject.Singleton;

@Singleton
public class TableColumnFactory {
    public TableColumn<SoundFile, String> getTitleColumn() {
        TableColumn<SoundFile, String> title = new TableColumn<>("Tytuł");
        title.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTitle()));
        return title;
    }

    public TableColumn<SoundFile, Integer> getRateColumn() {
        TableColumn<SoundFile, Integer> title = new TableColumn<>("Ocena");
        title.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getRate()));
        title.setCellFactory(param -> new TableCell<SoundFile, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(Rating.getStarFor(item != null ? item : 0));
            }
        });
        title.setPrefWidth(100);
        title.setMaxWidth(100);
        return title;
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
        return column;
    }
}