package net.kyma.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import lombok.AllArgsConstructor;
import net.kyma.EventType;
import net.kyma.dm.*;
import org.apache.commons.lang3.StringUtils;
import pl.khuzzuk.messaging.Bus;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;

import static net.kyma.EventType.DATA_UPDATE_REQUEST;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_CHANGED;
import static net.kyma.dm.SupportedField.COUNTER;
import static net.kyma.dm.SupportedField.RATE;

@AllArgsConstructor
public class TableColumnFactory
{
   private Bus<EventType> bus;

   private static final BiConsumer<SoundFile, TableCell<?, ?>> startFactory =
         (s, cell) -> Optional.ofNullable(s)
                 .map(SoundFile::getRate)
                 .map(Rating::getRate)
                 .map(Rating::getStarFor)
                 .ifPresent(cell::setGraphic);

   public TableColumn<SoundFile, ?> getColumnFor(SupportedField field, double width, Collection<String> suggestions)
   {
      switch (field)
      {
         case RATE:
            return getRateColumn();
         case COUNTER:
            return getCounterColumn(width);
         default:
            return suggestions != null
                  ? createStringColumnWithSuggestion(field, suggestions, width)
                  : createStringColumn(field, width);
      }
   }

   private TableColumn<SoundFile, SoundFile> getRateColumn()
   {
      TableColumn<SoundFile, SoundFile> column = new TableColumn<>(RATE.getName());
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
               if (e.getButton().equals(MouseButton.PRIMARY)) {
                  bus.message(DATA_UPDATE_REQUEST)
                        .withContent(new RateTagUpdateRequest(Rating.getFor((int) (e.getX() * 10 / getWidth() + 1))))
                        .send();
               }
            });
         }
      });
      column.setPrefWidth(100);
      column.setMaxWidth(100);
      return column;
   }

   private TableColumn<SoundFile, String> createStringColumn(SupportedField field, double width)
   {
      return createStringColumn(field, TextFieldTableCell.forTableColumn(), width);
   }

   private TableColumn<SoundFile, String> createStringColumnWithSuggestion(
         SupportedField field, Collection<String> values, double width)
   {
      return createStringColumn(field, any -> AutoCompleteTableCell.create(values), width);
   }

   private TableColumn<SoundFile, String> createStringColumn(SupportedField field,
         Callback<TableColumn<SoundFile, String>, TableCell<SoundFile, String>> cellFactory,
         double width)
   {
      TableColumn<SoundFile, String> column = new TableColumn<>(field.getName());
      column.setCellValueFactory(data -> new SimpleStringProperty(field.getGetter().apply(data.getValue())));
      column.setCellFactory(cellFactory);
      column.setOnEditCommit(value -> {
         field.getSetter().accept(value.getRowValue(), value.getNewValue());
         bus.message(DATA_UPDATE_REQUEST).withContent(new StringTagUpdateRequest(field, value.getNewValue())).send();
      });
      column.setPrefWidth(width);
      column.widthProperty().addListener((observable, newV, oldV) -> bus.message(GUI_CONTENTVIEW_SETTINGS_CHANGED).send());
      return column;
   }

   private TableColumn<SoundFile, Integer> getCounterColumn(double width)
   {
      TableColumn<SoundFile, Integer> column = new TableColumn<>(COUNTER.getName());
      column.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getCounter()));
      column.widthProperty().addListener((observable, newV, oldV) -> bus.message(GUI_CONTENTVIEW_SETTINGS_CHANGED).send());
      column.setPrefWidth(width);
      return column;
   }

   @SuppressWarnings("unchecked")
   private TableColumn<SoundFile, String> createTitleColumn()
   {
      TableColumn<SoundFile, String> column =
            createStringColumn(SupportedField.TITLE, TextFieldTableCell.forTableColumn(), 200);
      column.setCellValueFactory(param -> {
         if (!StringUtils.isEmpty(param.getValue().getTitle()))
         {
            return new SimpleStringProperty(param.getValue().getTitle());
         }
         else
         {
            String name = param.getValue().getFileName();
            return new SimpleObjectProperty(name.substring(0, name.lastIndexOf('.')));
         }
      });
      return column;
   }

   public TableColumn<SoundFile, String> getTitleColumnForPlaylist(IntegerProperty property)
   {
      TableColumn<SoundFile, String> column = createTitleColumn();
      column.setCellFactory(param -> new TableCell<>()
      {
         @Override
         protected void updateItem(String item, boolean empty)
         {
            super.updateItem(item, empty);
            Label label = new Label(item);
            getStyleClass().clear();
            if (getIndex() == property.get()) {
               getStyleClass().add("currentlyPlayed");
            } else {
               getStyleClass().addAll("cell", "indexed-cell", "table-cell", "table-column");
            }
            setGraphic(label);
         }
      });
      return column;
   }
}
