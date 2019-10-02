package net.kyma.gui.components;

import javafx.scene.control.TextField;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;

import java.util.Collection;
import java.util.stream.Collectors;

@UtilityClass
public class AutoCompletionUtils {
    public static void bindAutoCompletions(TextField textField, Collection<String> suggestions) {
        TextFields.bindAutoCompletion(textField, param -> suggestions.stream()
                .filter(element -> StringUtils.startsWithIgnoreCase(element, param.getUserText()))
                .collect(Collectors.toList()));
    }
}
