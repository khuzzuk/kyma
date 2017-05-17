package net.kyma.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import net.kyma.gui.PlayButton;
import net.kyma.player.PlaybackTimer;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

@SuppressWarnings("WeakerAccess")
@Singleton
public class PlayerPaneController implements Initializable {
    @FXML
    private PlayButton playButton;
    @FXML
    private Slider playbackProgress;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private PlaybackTimer timer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playbackProgress.setMajorTickUnit(0.01D);
        bus.<Long>setReaction(messages.getProperty("player.metadata.length"),
                i -> playbackProgress.setMax(i));
        bus.<Long>setReaction(messages.getProperty("player.metadata.currentTime"),
                i -> playbackProgress.setValue(i));

        playButton.showPlay();
    }

    @FXML
    private void startOrPause() {
        if (playButton.isPaused()) {
            playButton.showPlay();
            bus.send(messages.getProperty("player.pause.mp3"));
        } else {
            playButton.showPause();
            bus.send(messages.getProperty("player.resume"));
        }
    }

    @FXML
    private void stop() {
        bus.send(messages.getProperty("player.stop.mp3"));
        playButton.showPlay();
    }

    public void playFrom(MouseEvent mouseEvent) {
        bus.send(messages.getProperty("player.play.from.mp3"),
                Math.round(playbackProgress.getMax() * (mouseEvent.getX() / playbackProgress.getWidth())));
    }

    @FXML
    private void stopTimer() {
        timer.stop();
    }

    @FXML
    private void playNext() {
        bus.send(messages.getProperty("playlist.next"));
    }

    public void playPrevious() {
        bus.send(messages.getProperty("playlist.previous"));
    }
}
