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
    private Player currentPlayer;

    public void init() {
        timer.init();
        bus.setReaction(messages.getProperty("player.play.mp3"), this::playMp3);
        bus.setReaction(messages.getProperty("player.pause.mp3"), this::pauseMp3);
        bus.setReaction(messages.getProperty("player.stop.mp3"), this::stopMp3);
        bus.setReaction(messages.getProperty("close"), this::stopMp3);
        bus.setReaction(messages.getProperty("close"), FLACPlayer::closeFLACPlayers);
        bus.setReaction(messages.getProperty("player.metadata.getLength"),
                () -> bus.send(messages.getProperty("player.metadata.length"), currentPlayer.getLength()));
        bus.setReaction(messages.getProperty("player.metadata.getCurrentTime"),
                () -> bus.send(messages.getProperty("player.metadata.currentTime"), currentPlayer.playbackStatus()));
        bus.setReaction(messages.getProperty("player.play.from.mp3"), this::startFrom);
    }

    private synchronized void playMp3(SoundFile file) {
        if (currentPlayer != null) {
            if (currentPlayer.isPaused()) {
                currentPlayer.start();
                timer.start();
                return;
            } else {
                currentPlayer.stop();
            }
        }
        currentPlayer = file.getFormat().getPlayer(file, bus, messages);
        if (currentPlayer == null) {
            //TODO send communicate to the user
            return;
        }
        log.info("start play");
        currentPlayer.start();
        timer.start();
    }

    private synchronized void stopMp3() {
        timer.stop();
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
        }
    }

    private synchronized void pauseMp3() {
        timer.stop();
        currentPlayer.pause();
    }

    private synchronized void startFrom(Long millis) {
        if (currentPlayer != null) {
            currentPlayer.startFrom(millis);
            timer.start();
        }
    }
}
