package net.kyma.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.kyma.dm.MetadataField;
import net.kyma.dm.SoundFile;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.kyma.dm.MetadataField.*;

@Singleton
public class SoundFileEditor {
    private Stage window;
    private SoundFile soundFile;
    private ComboBox<Node> rate;
    private Map<MetadataField, TextField> fields;

    public void init() {
        Platform.runLater(() -> {
            window = new Stage();
            setUpFields();

            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(5, 5, 5, 5));

            MutableInt x = new MutableInt(0);
            fields.forEach((key, value) -> {
                gridPane.add(new Label(key.getName()), 0, x.getValue());
                gridPane.add(value, 1, x.getAndIncrement());
            });
            gridPane.add(rate, 2, 0);

            window.setScene(new Scene(gridPane));
        });
    }

    private void setUpFields() {
        rate = new ComboBox<>();
        rate.getItems().addAll(StarsFactory.defineForRating(0),
                StarsFactory.defineForRating(1),
                StarsFactory.defineForRating(2),
                StarsFactory.defineForRating(3),
                StarsFactory.defineForRating(4),
                StarsFactory.defineForRating(5),
                StarsFactory.defineForRating(6),
                StarsFactory.defineForRating(7),
                StarsFactory.defineForRating(8),
                StarsFactory.defineForRating(9),
                StarsFactory.defineForRating(10));

        fields = new LinkedHashMap<>();
        fields.put(TITLE, new TextField());
        fields.put(YEAR, new TextField());
        fields.put(ALBUM, new TextField());
        fields.put(ALBUM_ARTIST, new TextField());
        fields.put(ALBUM_ARTISTS, new TextField());
        fields.put(ARTIST, new TextField());
        fields.put(ARTISTS, new TextField());
        fields.put(COMPOSER, new TextField());
        fields.put(CONDUCTOR, new TextField());
        fields.put(COUNTRY, new TextField());
        fields.put(CUSTOM1, new TextField());
        fields.put(CUSTOM2, new TextField());
        fields.put(CUSTOM3, new TextField());
        fields.put(CUSTOM4, new TextField());
        fields.put(CUSTOM5, new TextField());
        fields.put(DISC_NO, new TextField());
        fields.put(GENRE, new TextField());
        fields.put(GROUP, new TextField());
        fields.put(INSTRUMENT, new TextField());
        fields.put(MOOD, new TextField());
        fields.put(MOVEMENT, new TextField());
        fields.put(OCCASION, new TextField());
        fields.put(OPUS, new TextField());
        fields.put(ORCHESTRA, new TextField());
        fields.put(QUALITY, new TextField());
        fields.put(RANKING, new TextField());
        fields.put(TEMPO, new TextField());
        fields.put(TONALITY, new TextField());
        fields.put(TRACK, new TextField());
        fields.put(WORK, new TextField());
        fields.put(WORK_TYPE, new TextField());
    }

    public void showEditor(SoundFile soundFile) {
        fields.values().forEach(TextField::clear);
        rate.getSelectionModel().clearSelection();
        this.soundFile = soundFile;
        fields.forEach((key, value) -> value.setText(key.getGetter().apply(this.soundFile)));
        rate.getSelectionModel().select(soundFile.getRate());
    }
}
