package net.kyma.gui;

import java.util.Collection;
import java.util.stream.Collectors;

import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;

public class AutoCompletionUtils
{
   static void bindAutoCompletions(TextField textField, Collection<String> suggestions)
   {
      TextFields.bindAutoCompletion(textField, param -> suggestions.stream()
            .filter(element -> StringUtils.startsWithIgnoreCase(element, param.getUserText()))
            .collect(Collectors.toList()));
   }
}
