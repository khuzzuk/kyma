package net.kyma.player;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import net.kyma.dm.SoundFile;

import java.nio.file.Paths;

public class Mp3PlayerFX extends Mp3Player {
    private MediaPlayer player;
    private MediaView mediaView;

    public Mp3PlayerFX(SoundFile file) {
        super(file);
        mediaView = new MediaView();
    }

    void start() {
        if (player == null) {
            Media sound = new Media(Paths.get(getPath()).toUri().toString());
            player = new MediaPlayer(sound);
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
        player.setStartTime(new Duration(millis));
        player.play();
    }
}
