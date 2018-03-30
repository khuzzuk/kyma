package net.kyma.gui.controllers;

import static java.lang.Math.round;
import static net.kyma.EventType.GUI_VOLUME_GET;
import static net.kyma.EventType.GUI_VOLUME_SET;
import static net.kyma.EventType.PLAYER_PAUSE;
import static net.kyma.EventType.PLAYER_PLAY_FROM;
import static net.kyma.EventType.PLAYER_RESUME;
import static net.kyma.EventType.PLAYER_SET_SLIDER;
import static net.kyma.EventType.PLAYER_SET_VOLUME;
import static net.kyma.EventType.PLAYER_STOP;
import static net.kyma.EventType.PLAYER_STOP_TIMER;
import static net.kyma.EventType.PLAYLIST_NEXT;
import static net.kyma.EventType.PLAYLIST_PREVIOUS;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import net.kyma.gui.PlayButton;
import pl.khuzzuk.messaging.Bus;

@SuppressWarnings("WeakerAccess")
@RequiredArgsConstructor
public class PlayerPaneController implements Initializable {
    @FXML
    private Slider volumeSlider;
    @FXML
    private PlayButton playButton;
    @FXML
    private Slider playbackProgress;
    private final Bus<EventType> bus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playbackProgress.setMajorTickUnit(0.01D);
        bus.subscribingFor(GUI_VOLUME_SET).<Integer>accept(value -> volumeSlider.setValue(value)).subscribe();
        bus.message(GUI_VOLUME_GET).withResponse(GUI_VOLUME_SET).send();
        bus.message(PLAYER_SET_SLIDER).withContent(playbackProgress).send();
        playButton.showPlay();
    }

    @FXML
    private void startOrPause() {
        if (playButton.isPaused()) {
            playButton.showPlay();
            bus.message(PLAYER_PAUSE).send();
        } else {
            playButton.showPause();
            bus.message(PLAYER_RESUME).send();
        }
    }

    @FXML
    private void stop() {
        bus.message(PLAYER_STOP).send();
        playButton.showPlay();
    }

    public void playFrom(MouseEvent mouseEvent) {
        bus.message(PLAYER_PLAY_FROM)
              .withContent(round(playbackProgress.getMax() * (mouseEvent.getX() / playbackProgress.getWidth())))
              .send();
    }

    public void setVolume(MouseEvent mouseEvent)
    {
        bus.message(PLAYER_SET_VOLUME).withContent((int) volumeSlider.getValue()).send();
    }

    @FXML
    private void stopTimer() {
        bus.message(PLAYER_STOP_TIMER).send();
    }

    @FXML
    private void playNext() {
        bus.message(PLAYLIST_NEXT).send();
    }

    public void playPrevious() {
        bus.message(PLAYLIST_PREVIOUS).send();
    }
}
