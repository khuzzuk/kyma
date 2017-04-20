package net.kyma.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
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
}
