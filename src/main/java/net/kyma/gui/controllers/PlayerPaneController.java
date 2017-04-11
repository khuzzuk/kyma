package net.kyma.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

@Singleton
public class PlayerPaneController implements Initializable {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void startOrResume() {
        bus.send(messages.getProperty("playlist.start"));
    }

    @FXML
    private void stop() {
        bus.send(messages.getProperty("player.stop.mp3"));
    }
}
