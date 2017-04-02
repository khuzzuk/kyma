package net.kyma.player;

import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

public class PlayerManager {
    private final Bus bus;
    private final Properties messages;
    private Mp3Player currentPlayer;

    @Inject
    public PlayerManager(Bus bus, Properties messages) {
        this.bus = bus;
        this.messages = messages;
    }

    public void init() throws IOException {
        bus.setReaction(messages.getProperty("player.play.mp3"), this::playMp3);
        bus.setReaction(messages.getProperty("player.metadata.getLength"), () -> System.out.println(currentPlayer.getLength()));
        bus.setReaction(messages.getProperty("player.metadata.getCurrentTime"), () -> System.out.println(currentPlayer.playbackStatus()));
    }

    private void playMp3(String path) {
        if (currentPlayer != null) {
            currentPlayer.stop();
        }
        currentPlayer = new Mp3Player(path);
        currentPlayer.initMetadata();
        currentPlayer.start();
    }
}
