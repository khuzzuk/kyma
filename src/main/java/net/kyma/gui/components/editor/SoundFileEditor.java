package net.kyma.gui.components.editor;

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
import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.gui.components.StarsFactory;
import net.kyma.gui.components.AutoCompletionUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import pl.khuzzuk.messaging.Bus;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static net.kyma.dm.SupportedField.ALBUM;
import static net.kyma.dm.SupportedField.ALBUM_ARTIST;
import static net.kyma.dm.SupportedField.ALBUM_ARTISTS;
import static net.kyma.dm.SupportedField.ARTIST;
import static net.kyma.dm.SupportedField.ARTISTS;
import static net.kyma.dm.SupportedField.COMPOSER;
import static net.kyma.dm.SupportedField.CONDUCTOR;
import static net.kyma.dm.SupportedField.COUNTRY;
import static net.kyma.dm.SupportedField.CUSTOM1;
import static net.kyma.dm.SupportedField.CUSTOM2;
import static net.kyma.dm.SupportedField.CUSTOM3;
import static net.kyma.dm.SupportedField.CUSTOM4;
import static net.kyma.dm.SupportedField.CUSTOM5;
import static net.kyma.dm.SupportedField.DISC_NO;
import static net.kyma.dm.SupportedField.FILE_NAME;
import static net.kyma.dm.SupportedField.GENRE;
import static net.kyma.dm.SupportedField.GROUP;
import static net.kyma.dm.SupportedField.INSTRUMENT;
import static net.kyma.dm.SupportedField.MOOD;
import static net.kyma.dm.SupportedField.MOVEMENT;
import static net.kyma.dm.SupportedField.OCCASION;
import static net.kyma.dm.SupportedField.OPUS;
import static net.kyma.dm.SupportedField.ORCHESTRA;
import static net.kyma.dm.SupportedField.QUALITY;
import static net.kyma.dm.SupportedField.RANKING;
import static net.kyma.dm.SupportedField.TEMPO;
import static net.kyma.dm.SupportedField.TITLE;
import static net.kyma.dm.SupportedField.TONALITY;
import static net.kyma.dm.SupportedField.TRACK;
import static net.kyma.dm.SupportedField.WORK;
import static net.kyma.dm.SupportedField.WORK_TYPE;
import static net.kyma.dm.SupportedField.YEAR;

@RequiredArgsConstructor
public class SoundFileEditor
{
   final Bus<EventType> bus;
   Stage window;
   private SoundFile soundFile;
   private ComboBox<Node> rate;
   Map<SupportedField, TextField> fields;

   public void init(Map<SupportedField, Collection<String>> suggestions)
   {
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

         fields.forEach((field, textField) -> {
            if (x.intValue() > 0 && x.intValue() > 15)
            {
               max.setValue(x.intValue());
               x.setValue(0);
               y.add(2);
            }
            Label label = new Label(field.getName());
            label.getStyleClass().add("flowing");
            if (suggestions.containsKey(field))
            {
               AutoCompletionUtils.bindAutoCompletions(textField, suggestions.get(field));
            }
            gridPane.add(label, y.intValue(), x.intValue());
            gridPane.add(textField, y.intValue() + 1, x.getAndIncrement());
         });

         Label rating = new Label("ocena");
         rating.getStyleClass().add("flowing");
         gridPane.add(rating, y.intValue(), x.incrementAndGet());
         gridPane.add(rate, y.incrementAndGet(), x.intValue());
         Button ok = new Button("ok");
         ok.setOnAction(e -> saveSoundFile());
         gridPane.add(ok, y.intValue(), Math.max(max.incrementAndGet(), x.incrementAndGet()));

         Scene scene = new Scene(gridPane);
         scene.getStylesheets().add("/css/classic.css");
         window.setScene(scene);
         window.setOnCloseRequest(e -> window.hide());
      });
   }

   private void setUpFields()
   {
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

      fields = new EnumMap<>(SupportedField.class);
      fields.put(FILE_NAME, createNew(false));
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

   private TextField createNew()
   {
      return createNew(true);
   }

   private TextField createNew(boolean editable)
   {
      TextField textField = new TextField();
      textField.setPrefWidth(200);
      textField.setEditable(editable);
      return textField;
   }

   public void showEditor(SoundFile soundFile)
   {
      fields.values().forEach(TextField::clear);
      rate.getSelectionModel().clearSelection();
      this.soundFile = soundFile;
      fields.forEach((key, value) -> value.setText(key.getGetter().apply(this.soundFile)));
      rate.getSelectionModel().select(soundFile.getRate().getRate());
      window.setTitle(soundFile.getFileName());
      window.show();
   }

   void saveSoundFile()
   {
      fields.forEach((field, value) -> field.getSetter().accept(soundFile, value.getText()));
      soundFile.setRate(Rating.getFor(rate.getSelectionModel().getSelectedIndex()));
      bus.message(EventType.DATA_STORE_ITEM).withContent(soundFile).send();
      window.hide();
      soundFile = null;
   }
}
