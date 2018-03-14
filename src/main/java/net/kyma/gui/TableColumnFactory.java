package net.kyma.gui;

import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.DATA_UPDATE_REQUEST;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_CHANGED;
import static net.kyma.dm.SupportedField.COUNTER;
import static net.kyma.dm.SupportedField.RATE;
import static net.kyma.dm.SupportedField.TITLE;

import java.util.Collection;
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
import javafx.util.Callback;
import lombok.AllArgsConstructor;
import net.kyma.EventType;
import net.kyma.dm.Rating;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.dm.TagUpdateRequest;
import org.apache.commons.lang3.StringUtils;
import pl.khuzzuk.messaging.Bus;

@AllArgsConstructor
public class TableColumnFactory
{
   private Bus<EventType> bus;

   private static BiConsumer<SoundFile, TableCell<?, ?>> startFactory =
         (s, cell) -> Optional.ofNullable(s).map(Rating::getStarFor).ifPresent(cell::setGraphic);

   public TableColumn<SoundFile, ?> getColumnFor(SupportedField field, double width)
   {
      return getColumnFor(field, width, null);
   }

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

   public TableColumn<SoundFile, SoundFile> getRateColumn()
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

   private TableColumn<SoundFile, String> createStringColumn(SupportedField field, double width)
   {
      return createStringColumn(field, TextFieldTableCell.forTableColumn(), width);
   }

   private TableColumn<SoundFile, String> createStringColumnWithSuggestion(
         SupportedField field, Collection<String> values, double width)
   {
      return createStringColumn(field, __ -> AutoCompleteTableCell.create(values), width);
   }

   private TableColumn<SoundFile, String> createStringColumn(SupportedField field,
         Callback<TableColumn<SoundFile, String>, TableCell<SoundFile, String>> cellFactory,
         double width)
   {
      TableColumn<SoundFile, String> column = new TableColumn<>(field.getName());
      column.setCellValueFactory(data -> new SimpleStringProperty(field.getGetter().apply(data.getValue())));
      column.setCellFactory(cellFactory);
      column.setOnEditCommit(value ->
            bus.send(DATA_UPDATE_REQUEST, new TagUpdateRequest(field, value.getNewValue())));
      column.setPrefWidth(width);
      column.widthProperty().addListener((_1, _2, _3) -> bus.send(GUI_CONTENTVIEW_SETTINGS_CHANGED));
      return column;
   }

   public TableColumn<SoundFile, Integer> getCounterColumn(double width)
   {
      TableColumn<SoundFile, Integer> column = new TableColumn<>(COUNTER.getName());
      column.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getCounter()));
      column.widthProperty().addListener((_1, _2, _3) -> bus.send(GUI_CONTENTVIEW_SETTINGS_CHANGED));
      column.setPrefWidth(width);
      return column;
   }

   public TableColumn<SoundFile, String> createTitleColumn()
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
            return new SimpleObjectProperty(name.substring(0, name.lastIndexOf(".")));
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
            if (getIndex() == property.get())
            {
               getStyleClass().clear();
               getStyleClass().add("currentlyPlayed");
            }
            else
            {
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
