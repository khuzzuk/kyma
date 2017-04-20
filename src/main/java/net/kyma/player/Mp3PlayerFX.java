package net.kyma.player;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import java.nio.file.Paths;
import java.util.Properties;

public class Mp3PlayerFX extends Mp3Player {
    private MediaPlayer player;
    private MediaView mediaView;
    private Bus bus;
    private Properties messages;

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

    private void hold() {
        while (player.getStatus() != MediaPlayer.Status.PAUSED) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
