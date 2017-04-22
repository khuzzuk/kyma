package net.kyma.gui;


import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class StopButton extends Button {
    public StopButton() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        rectangle.setFill(Color.GREY);
        rectangle.setStroke(null);
        setGraphic(new Group(rectangle));
    }
}
