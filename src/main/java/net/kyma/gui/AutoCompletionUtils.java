package net.kyma.gui;

import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class AutoCompletionUtils {
    static void bindAutoCompletions(TextField textField, Collection<String> suggestions) {
        ContextMenu suggestionsContextMenu = new ContextMenu();

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                suggestionsContextMenu.hide();
            } else {
                List<String> validSuggestions = suggestions.stream().filter(value -> StringUtils.startsWithIgnoreCase(value, newValue))
                        .collect(Collectors.toList());
                populateMenu(textField, suggestionsContextMenu, validSuggestions);
                if (!suggestionsContextMenu.getItems().isEmpty()) {
                    suggestionsContextMenu.show(textField, Side.BOTTOM, 0, 0);
                }
            }
        });
    }

    private static void populateMenu(TextField textField, ContextMenu menu, Collection<String> values) {
        ObservableList<MenuItem> menuItems = menu.getItems();
        menuItems.clear();
        for (String value : values) {
            Label label = new Label(value);
            CustomMenuItem customMenuItem = new CustomMenuItem(label);
            customMenuItem.setOnAction(event -> onClick(textField, value));
            menuItems.add(customMenuItem);
        }
    }

    private static void onClick(TextField textField, String result) {
        textField.setText(result);
    }
}
