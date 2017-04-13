package net.kyma.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
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
    private Slider playbackProgress;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playbackProgress.setMajorTickUnit(0.01D);
        bus.<Long>setReaction(messages.getProperty("player.metadata.length"),
                i -> playbackProgress.setMax(i));
        bus.<Long>setReaction(messages.getProperty("player.metadata.currentTime"),
                i -> playbackProgress.setValue(i));
    }

    @FXML
    private void startOrResume() {
        bus.send(messages.getProperty("playlist.start"));
    }

    @FXML
    private void stop() {
        bus.send(messages.getProperty("player.stop.mp3"));
    }

    @FXML
    private void playFrom() {
        bus.send(messages.getProperty("playlist.start.from"), Math.round(playbackProgress.getValue()));
    }
}
