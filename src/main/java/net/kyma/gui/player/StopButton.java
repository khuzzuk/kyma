package net.kyma.gui.player;


import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;

public class StopButton extends Button {
    public StopButton() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        rectangle.getStyleClass().add("play-text");
        setGraphic(new Group(rectangle));

        getStyleClass().add("roundButton");
    }
}
