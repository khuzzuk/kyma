package net.kyma.gui.player;

import static net.kyma.gui.components.GridPaneUtils.columnConstraints;
import static net.kyma.gui.components.GridPaneUtils.rowConstraints;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;

public class PlayerPane extends GridPane {
    public PlayerPane(PlayerPaneController playerPaneController) {
        getColumnConstraints().addAll(columnConstraints(2),
                                      columnConstraints(18),
                                      columnConstraints(75),
                                      columnConstraints(5));
        RowConstraints rowConstraints = rowConstraints(0);
        rowConstraints.setValignment(VPos.CENTER);
        getRowConstraints().addAll(rowConstraints);
        setPadding(new Insets(5, 10, 5, 10));
        setHgap(5);
        setVgap(5);

        Slider volumeSlider = new Slider();
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setOnMouseClicked(playerPaneController::setVolume);

        PlayButton playButton = new PlayButton();
        playButton.setOnMouseClicked(event -> playerPaneController.startOrPause());
        StopButton stopButton = new StopButton();
        stopButton.setOnMouseClicked(event -> playerPaneController.stop());
        PreviousPlayButton previousPlayButton = new PreviousPlayButton();
        previousPlayButton.setOnMouseClicked(event -> playerPaneController.playPrevious());
        NextPlayButton nextPlayButton = new NextPlayButton();
        nextPlayButton.setOnMouseClicked(event -> playerPaneController.playNext());

        HBox playerControllers = new HBox(playButton, stopButton, previousPlayButton, nextPlayButton);
        playerControllers.setSpacing(5);
        playerControllers.setAlignment(Pos.CENTER);

        Slider playbackProgress = new Slider();
        playbackProgress.setOnMouseClicked(playerPaneController::playFrom);

        Label timeLabel = new Label();

        add(volumeSlider, 0, 0);
        add(playerControllers, 1, 0);
        add(playbackProgress, 2, 0);
        add(timeLabel, 3, 0);

        playerPaneController.setPlaybackProgress(playbackProgress);
        playerPaneController.setPlayButton(playButton);
        playerPaneController.setVolumeSlider(volumeSlider);
        playerPaneController.setTimeLabel(timeLabel);
        playerPaneController.initialize();
    }
}
