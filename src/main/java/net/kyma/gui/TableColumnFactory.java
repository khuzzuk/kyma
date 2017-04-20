package net.kyma.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.kyma.dm.SoundFile;

import javax.inject.Singleton;

@Singleton
public class TableColumnFactory {
    public TableColumn<SoundFile, String> getTitleColumn() {
        TableColumn<SoundFile, String> title = new TableColumn<>("Tytuł");
        title.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTitle()));
        return title;
    }

    public TableColumn<SoundFile, String> getRateColumn() {
        TableColumn<SoundFile, String> title = new TableColumn<>("Ocena");
        title.setCellValueFactory(p -> new SimpleStringProperty("" + p.getValue().getRate()));
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
