package net.kyma.player;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import java.nio.file.Paths;
import java.util.Properties;

@Log4j2
public class Mp3PlayerFX extends Mp3Player {
    private MediaPlayer player;
    private MediaView mediaView;
    private Bus bus;
    private Properties messages;
    private long length;

    public Mp3PlayerFX(SoundFile file, Bus bus, Properties messages) {
        super(file);
        this.bus = bus;
        this.messages = messages;
        mediaView = new MediaView();
    }

    void start() {
        if (player == null) {
            Media sound = new Media(Paths.get(getPath()).toUri().toString());
            player = new MediaPlayer(sound);
            player.setOnEndOfMedia(() -> bus.send(messages.getProperty("playlist.next")));
            length = calculateLength(player);
        }
        mediaView.setMediaPlayer(player);
        setStartedTime(System.currentTimeMillis());
        player.play();
    }

    void stop() {
        player.stop();
        player = null;
    }

    void pause() {
        player.pause();
    }

    boolean isPaused() {
        return player.getStatus() == MediaPlayer.Status.PAUSED;
    }

    @Override
    long playbackStatus() {
        return Math.round(player.getCurrentTime().toMillis());
    }

    void startFrom(long millis) {
        player.pause();
        hold();
        player.setStartTime(new Duration(millis));
        player.play();
    }

    private long calculateLength(MediaPlayer player) {
        if (player == null) return 0;
        while (Double.valueOf(Double.NaN).equals(player.getTotalDuration().toMillis())) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        return Math.round(player.getTotalDuration().toMillis());
    }

    private void hold() {
        while (player.getStatus() != MediaPlayer.Status.PAUSED) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }

    @Override
    synchronized long getLength() {
        return length;
    }
}
