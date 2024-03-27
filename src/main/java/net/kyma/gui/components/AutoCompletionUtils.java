package net.kyma.gui.components;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AutoCompletionUtils {
    public static void bindAutoCompletions(TextField textField, Collection<String> suggestions) {
        ContextMenu suggestionsContextMenu = new ContextMenu();

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                suggestionsContextMenu.hide();
            } else {
                List<String> validSuggestions = suggestions.stream()
                    .filter(value -> startsWithIgnoreCase(value, newValue))
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
            customMenuItem.setOnAction(event -> textField.setText(value));
            menuItems.add(customMenuItem);
        }
    }
}
