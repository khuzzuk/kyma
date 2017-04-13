package net.kyma.player;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

@Log4j2
public class PlayerManager {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private PlaybackTimer timer;
    private Mp3PlayerFX currentPlayer;

    public void init() {
        timer.init();
        bus.setReaction(messages.getProperty("player.play.mp3"), this::playMp3);
        bus.setReaction(messages.getProperty("player.stop.mp3"), this::stopMp3);
        bus.setReaction(messages.getProperty("close"), this::stopMp3);
        bus.setReaction(messages.getProperty("player.metadata.getLength"),
                () -> bus.send(messages.getProperty("player.metadata.length"), currentPlayer.getLength()));
        bus.setReaction(messages.getProperty("player.metadata.getCurrentTime"),
                () -> bus.send(messages.getProperty("player.metadata.currentTime"), currentPlayer.playbackStatus()));
    }

    private void playMp3(SoundFile file) {
        if (currentPlayer != null) {
            currentPlayer.stop();
        }
        currentPlayer = new Mp3PlayerFX(file);
        currentPlayer.initMetadata();
        log.info("start play");
        timer.start();
        currentPlayer.start();
    }

    private void stopMp3() {
        timer.stop();
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
        }
    }
}
