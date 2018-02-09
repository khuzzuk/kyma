package net.kyma.player;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import pl.khuzzuk.messaging.Bus;

@Log4j2
class Mp3PlayerFX implements Player {
    private AudioInputStream player;
    private final Bus bus;
    private final Properties messages;
    @Getter
    private long length;
    @Getter
    private final String path;
    @Getter
    private boolean paused;

    Mp3PlayerFX(SoundFile file, Bus bus, Properties messages) {
        this.path = file.getPath();
        this.bus = bus;
        this.messages = messages;
    }

    @Override
    public void start() {
        if (player == null) {
            try
            {
                AudioInputStream rawAudio = AudioSystem.getAudioInputStream(new File(path));
                AudioFormat audioFormat = player.getFormat();
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
                      audioFormat.getChannels(), audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false);
                player = AudioSystem.getAudioInputStream(format, rawAudio);
                length = calculateLength(player);
                player.play();
            }
            catch (UnsupportedAudioFileException | IOException e)
            {
                e.printStackTrace();
            }
        }
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
        player.stop();
        paused = true;
    }

    @Override
    public long playbackStatus() {
        return Math.round(Optional.ofNullable(player).map(p -> p.getCurrentTime().toMillis()).orElse(0D));
    }

    @Override
    public void startFrom(long millis) {
        player.stop();
        hold();
        player.setStartTime(new Duration(millis));
        player.play();
    }

    long calculateLength() throws IOException, UnsupportedAudioFileException {
        if (player == null) return 0;
        TAudioFileFormat format = (TAudioFileFormat) AudioSystem.getAudioFileFormat(new File(path));
        return (long) format.properties().get("duration") / 1000; //microseconds
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

    private class InternalPlayer {
        
    }
}
