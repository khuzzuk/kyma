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
import java.util.Optional;
import java.util.Properties;

@Log4j2
class Mp3PlayerFX implements Player {
    private MediaPlayer player;
    private final MediaView mediaView;
    private final Bus bus;
    private final Properties messages;
    @Getter
    private long length;
    @Getter
    private final String path;

    Mp3PlayerFX(SoundFile file, Bus bus, Properties messages) {
        this.path = file.getPath();
        this.bus = bus;
        this.messages = messages;
        mediaView = new MediaView();
    }

    @Override
    public void start() {
        if (player == null) {
            Media sound = new Media(Paths.get(path).toUri().toString());
            player = new MediaPlayer(sound);
            player.setOnEndOfMedia(() -> bus.send(messages.getProperty("playlist.next")));
            length = calculateLength(player);
        }
        mediaView.setMediaPlayer(player);
        player.play();
    }

    @Override
    public void stop() {
        if (player != null) {
            player.stop();
        }
        player = null;
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public boolean isPaused() {
        return player.getStatus() == MediaPlayer.Status.PAUSED;
    }

    @Override
    public long playbackStatus() {
        return Math.round(Optional.ofNullable(player).map(p -> p.getCurrentTime().toMillis()).orElse(0D));
    }

    @Override
    public void startFrom(long millis) {
        player.pause();
        hold();
        player.setStartTime(new Duration(millis));
        player.play();
    }

    long calculateLength(MediaPlayer player) {
        if (player == null) return 0;
        int retries = 100;
        int currentRetry = 0;
        while (Double.valueOf(Double.NaN).equals(player.getTotalDuration().toMillis())) {
            try {
                Thread.sleep(4);
                currentRetry++;
                if (currentRetry >= retries) break;
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
}
