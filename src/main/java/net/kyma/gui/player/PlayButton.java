package net.kyma.gui.player;

import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

public class PlayButton extends Button {
    private Group play;
    private Group pause;
    private boolean paused;
    private static final String PLAY_BUTTON_STYLE = "play-text";

    public PlayButton() {
        Path triangle = new Path();
        triangle.getElements().addAll(
                new MoveTo(2, 0),
                new LineTo(12, 5),
                new LineTo(2, 10),
                new ClosePath()
        );
        triangle.getStyleClass().add(PLAY_BUTTON_STYLE);
        play = new Group(triangle);

        Rectangle first = new Rectangle(0, 0, 4, 10);
        first.getStyleClass().add(PLAY_BUTTON_STYLE);
        Rectangle second = new Rectangle(6, 0, 4, 10);
        second.getStyleClass().add(PLAY_BUTTON_STYLE);
        pause = new Group(first, second);

        getStyleClass().add("roundButton");
    }

    public void showPlay() {
        paused = false;
        setGraphic(play);
    }

    public void showPause() {
        paused = true;
        setGraphic(pause);
    }

    public boolean isPaused() {
        return paused;
    }
}
