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
        bus.setReaction(GUI_VOLUME_SET, this::setVolumeSliderValue);
        bus.sendMessage(GUI_VOLUME_GET, GUI_VOLUME_SET);
        bus.send(PLAYER_SET_SLIDER, playbackProgress);
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

    public void setVolume(MouseEvent mouseEvent)
    {
        double sliderValue = volumeSlider.getValue();
        bus.send(PLAYER_SET_VOLUME, (int) (sliderValue * (sliderValue / 100d)));
    }

    public void setVolumeSliderValue(double sliderValue)
    {
        volumeSlider.setValue(Math.sqrt(sliderValue * 100));
        bus.send(PLAYER_SET_VOLUME, (int) sliderValue);
    }

    @FXML
    private void stopTimer() {
        bus.send(PLAYER_STOP_TIMER);
    }

    @FXML
    private void playNext() {
        bus.send(PLAYLIST_NEXT);
    }

    public void playPrevious() {
        bus.send(PLAYLIST_PREVIOUS);
    }
}
