package net.kyma.gui;

import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

public class ClassicTheme {
    public static Scene style(Scene scene) {
        //scene.getStylesheets().add(ClassicTheme.class.getResource("/net/kyma/gui/classic.css").toExternalForm());
        return scene;
    }

    private static Background background(Color color) {
        return new Background(new BackgroundFill(color, null, null));
    }

    private static Color BASIC_COLOR = Color.rgb(229, 219, 205);
    private static Color SECOND_COLOR = Color.rgb(186, 177, 167, 0.6);
}
