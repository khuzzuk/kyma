package net.kyma.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

@Singleton
public class PlayerPaneController implements Initializable {
    @FXML
    private Button playButton;
    @FXML
    private Slider playbackProgress;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    private static final String play = "►";
    private static final String pause = "‖‖";
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playbackProgress.setMajorTickUnit(0.01D);
        bus.<Long>setReaction(messages.getProperty("player.metadata.length"),
                i -> playbackProgress.setMax(i));
        bus.<Long>setReaction(messages.getProperty("player.metadata.currentTime"),
                i -> playbackProgress.setValue(i));
    }

    @FXML
    private void startOrPause() {
        if (playButton.getText().equals(pause)) {
            playButton.setText(play);
            bus.send(messages.getProperty("player.pause.mp3"));
        } else {
            playButton.setText(pause);
            bus.send(messages.getProperty("playlist.start"));
        }
    }

    @FXML
    private void stop() {
        bus.send(messages.getProperty("player.stop.mp3"));
        playButton.setText(play);
    }

    public void playFrom(MouseEvent mouseEvent) {
        bus.send(messages.getProperty("player.play.from.mp3"),
                Math.round(playbackProgress.getMax() * (mouseEvent.getX() / playbackProgress.getWidth())));
    }
}
