package net.kyma.gui;

import java.util.Collection;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import net.kyma.dm.SoundFile;
import org.apache.commons.lang3.StringUtils;

public class AutoCompleteTableCell extends TextFieldTableCell<SoundFile, String>
{
   private Collection<String> suggestions;

   public AutoCompleteTableCell(Collection<String> suggestions)
   {
      super(new DefaultStringConverter());
      this.suggestions = suggestions;

      textProperty().addListener((observable, oldvalue, newValue) -> {
         if (StringUtils.isNoneBlank(newValue))
         {

         }
      });
   }

   private void showPopupFor(String value)
   {

   }
}
