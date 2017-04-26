package net.kyma.gui;

import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class PlayButton extends Button {
    private Group play;
    private Group pause;
    private boolean paused;

    public void showPlay() {
        if (play == null) {
            Path triangle = new Path();
            triangle.getElements().addAll(
                    new MoveTo(2, 0),
                    new LineTo(12, 5),
                    new LineTo(2, 10),
                    new ClosePath()
            );
            triangle.setFill(Color.GREY);
            triangle.setStroke(null);
            play = new Group(triangle);
        }
        paused = false;
        setGraphic(play);
    }

    public void showPause() {
        if (pause == null) {
            Rectangle first = new Rectangle(0, 0, 4, 10);
            first.setFill(Color.GREY);
            first.setStroke(null);
            Rectangle second = new Rectangle(6, 0, 4, 10);
            second.setFill(Color.GREY);
            second.setStroke(null);
            pause = new Group(first, second);
        }
        paused = true;
        setGraphic(pause);
    }

    public boolean isPaused() {
        return paused;
    }
}
