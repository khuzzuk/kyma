package net.kyma.player;

import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

public class PlayerManager {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    private Mp3PlayerJLayer currentPlayer;

    public void init() {
        bus.setReaction(messages.getProperty("player.play.mp3"), this::playMp3);
        bus.setReaction(messages.getProperty("player.stop.mp3"), this::stopMp3);
        bus.setReaction(messages.getProperty("close"), this::stopMp3);
        bus.setReaction(messages.getProperty("player.metadata.getLength"), () -> System.out.println(currentPlayer.getLength()));
        bus.setReaction(messages.getProperty("player.metadata.getCurrentTime"), () -> System.out.println(currentPlayer.playbackStatus()));
    }

    private void playMp3(SoundFile file) {
        if (currentPlayer != null) {
            currentPlayer.stop();
        }
        currentPlayer = new Mp3PlayerJLayer(file);
        currentPlayer.initMetadata();
        currentPlayer.start();
    }

    private void stopMp3() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
        }
    }
}
