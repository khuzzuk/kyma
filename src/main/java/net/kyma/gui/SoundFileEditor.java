package net.kyma.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    Stage window;
    private SoundFile soundFile;
    private ComboBox<Node> rate;
    Map<MetadataField, TextField> fields;

    public void init() {
        Platform.runLater(() -> {
            window = new Stage();
            setUpFields();

            GridPane gridPane = new GridPane();
            gridPane.setId("BackgroundContainer");
            gridPane.setPadding(new Insets(5, 5, 5, 5));
            gridPane.setHgap(5);
            gridPane.setVgap(5);

            MutableInt x = new MutableInt(0);
            MutableInt y = new MutableInt(0);
            MutableInt max = new MutableInt(0);
            fields.forEach((key, value) -> {
                if (x.intValue() > 0 && x.intValue() > 15) {
                    max.setValue(x.intValue());
                    x.setValue(0);
                    y.add(2);
                }
                gridPane.add(new Label(key.getName()), y.intValue(), x.intValue());
                gridPane.add(value, y.intValue() + 1, x.getAndIncrement());
            });
            gridPane.add(new Label("ocena"), y.intValue(), x.incrementAndGet());
            gridPane.add(rate, y.incrementAndGet(), x.intValue());
            gridPane.add(new Button("ok"), y.intValue(), Math.max(max.incrementAndGet(), x.incrementAndGet()));

            Scene scene = new Scene(gridPane);
            scene.getStylesheets().add("/css/player.css");
            window.setScene(scene);
            window.setOnCloseRequest(e -> window.hide());
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
        fields.put(TITLE, createNew());
        fields.put(YEAR, createNew());
        fields.put(ALBUM, createNew());
        fields.put(ALBUM_ARTIST, createNew());
        fields.put(ALBUM_ARTISTS, createNew());
        fields.put(ARTIST, createNew());
        fields.put(ARTISTS, createNew());
        fields.put(COMPOSER, createNew());
        fields.put(CONDUCTOR, createNew());
        fields.put(COUNTRY, createNew());
        fields.put(CUSTOM1, createNew());
        fields.put(CUSTOM2, createNew());
        fields.put(CUSTOM3, createNew());
        fields.put(CUSTOM4, createNew());
        fields.put(CUSTOM5, createNew());
        fields.put(DISC_NO, createNew());
        fields.put(GENRE, createNew());
        fields.put(GROUP, createNew());
        fields.put(INSTRUMENT, createNew());
        fields.put(MOOD, createNew());
        fields.put(MOVEMENT, createNew());
        fields.put(OCCASION, createNew());
        fields.put(OPUS, createNew());
        fields.put(ORCHESTRA, createNew());
        fields.put(QUALITY, createNew());
        fields.put(RANKING, createNew());
        fields.put(TEMPO, createNew());
        fields.put(TONALITY, createNew());
        fields.put(TRACK, createNew());
        fields.put(WORK, createNew());
        fields.put(WORK_TYPE, createNew());
    }

    private TextField createNew() {
        TextField textField = new TextField();
        textField.setPrefWidth(200);
        return textField;
    }

    public void showEditor(SoundFile soundFile) {
        fields.values().forEach(TextField::clear);
        rate.getSelectionModel().clearSelection();
        this.soundFile = soundFile;
        fields.forEach((key, value) -> value.setText(key.getGetter().apply(this.soundFile)));
        rate.getSelectionModel().select(soundFile.getRate());
        window.show();
    }
}
