package net.kyma.gui;


import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.shape.*;

public class PreviousPlayButton extends Button {
    public PreviousPlayButton() {
        Group group = new Group(constructTriangle(0), constructTriangle(5));
        setGraphic(new Group(group));
    }

    private Path constructTriangle(int shift) {
        Path triangle = new Path();
        triangle.getElements().addAll(new MoveTo(shift, 0),
                new LineTo(5 + shift, -5),
                new LineTo(5 + shift, 5),
                new ClosePath());
        triangle.getStyleClass().add("play-text");
        return triangle;
    }
}
