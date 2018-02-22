package net.kyma.gui.controllers;

import static java.lang.Math.round;
import static net.kyma.EventType.METADATA_LENGTH;
import static net.kyma.EventType.METADATA_TIME_CURRENT;
import static net.kyma.EventType.PLAYER_PAUSE;
import static net.kyma.EventType.PLAYER_PLAY_FROM;
import static net.kyma.EventType.PLAYER_RESUME;
import static net.kyma.EventType.PLAYER_STOP;
import static net.kyma.EventType.PLAYLIST_NEXT;
import static net.kyma.EventType.PLAYLIST_PREVIOUS;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import net.kyma.EventType;
import net.kyma.gui.PlayButton;
import net.kyma.player.PlaybackTimer;
import pl.khuzzuk.messaging.Bus;

@SuppressWarnings("WeakerAccess")
public class PlayerPaneController implements Initializable {
    @FXML
    private PlayButton playButton;
    @FXML
    private Slider playbackProgress;
    private Bus<EventType> bus;
    private PlaybackTimer timer;

    public PlayerPaneController(Bus<EventType> bus, PlaybackTimer timer)
    {
        this.bus = bus;
        this.timer = timer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playbackProgress.setMajorTickUnit(0.01D);
        bus.setReaction(METADATA_LENGTH, (Long max) -> playbackProgress.setMax(max));
        bus.setReaction(METADATA_TIME_CURRENT, (Long current) -> playbackProgress.setValue(current));
        playButton.showPlay();
    }

    @FXML
    private void startOrPause() {
        if (playButton.isPaused()) {
            playButton.showPlay();
            bus.send(PLAYER_PAUSE);
        } else {
            playButton.showPause();
            bus.send(PLAYER_RESUME);
        }
    }

    @FXML
    private void stop() {
        bus.send(PLAYER_STOP);
        playButton.showPlay();
    }

    public void playFrom(MouseEvent mouseEvent) {
        bus.send(PLAYER_PLAY_FROM, round(playbackProgress.getMax() * (mouseEvent.getX() / playbackProgress.getWidth())));
    }

    @FXML
    private void stopTimer() {
        timer.stop();
    }

    @FXML
    private void playNext() {
        bus.send(PLAYLIST_NEXT);
    }

    public void playPrevious() {
        bus.send(PLAYLIST_PREVIOUS);
    }
}
