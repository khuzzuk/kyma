package net.kyma.gui;

import java.util.Collection;
import java.util.stream.Collectors;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import net.kyma.dm.SoundFile;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;

public class AutoCompleteTableCell extends TableCell<SoundFile, String>
{
   private Collection<String> suggestions;
   private TextField editableField;

   private AutoCompleteTableCell(Collection<String> suggestions)
   {
      this.suggestions = suggestions;
      this.getStyleClass().add("text-field-table-cell");
   }

   public static AutoCompleteTableCell create(Collection<String> suggestions)
   {
      AutoCompleteTableCell autoCompleteTableCell = new AutoCompleteTableCell(suggestions);
      autoCompleteTableCell.load();
      return autoCompleteTableCell;
   }

   @Override
   public void updateItem(String item, boolean empty)
   {
      super.updateItem(item, empty);
      if (isEmpty())
      {
         setText(null);
         setGraphic(null);
      }
      else
      {
         if (isEditing())
         {
            editableField.setText(getText());
            setText(null);
         }
         else
         {
            setGraphic(null);
            setText(item);
         }
      }
   }

   private void load()
   {
      editableField = new TextField();
      AutoCompletionUtils.bindAutoCompletions(editableField, suggestions);
      editableField.setOnAction(event -> {
         commitEdit(editableField.getText());
         event.consume();
      });
   }

   @Override
   public void startEdit()
   {
      if (!isEditable()
            || !getTableView().isEditable()
            || !getTableColumn().isEditable())
      {
         return;
      }
      super.startEdit();
      editableField.setText(getText());
      setText(null);
      setGraphic(editableField);
      editableField.selectAll();
      editableField.requestFocus();
   }

   @Override
   public void cancelEdit()
   {
      super.cancelEdit();
      setGraphic(null);
   }

   @Override
   public void commitEdit(String newValue)
   {
      super.commitEdit(newValue);
      suggestions.add(newValue);
   }
}
