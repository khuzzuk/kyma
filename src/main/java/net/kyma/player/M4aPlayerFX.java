package net.kyma.player;

import java.io.File;
import java.io.IOException;

import javafx.scene.media.MediaPlayer;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import pl.khuzzuk.messaging.Bus;

@Log4j2
class M4aPlayerFX extends Mp3PlayerFX {
    M4aPlayerFX(SoundFile file, Bus<EventType> bus) {
        super(file, bus);
    }

    @Override
    long calculateLength(MediaPlayer player) {
        if (player == null) return 0;
        int retry = 20;
        int currentlyRetried = 0;
        while (Double.valueOf(Double.NaN).equals(player.getTotalDuration().toMillis())) {
            try {
                Thread.sleep(4);
                currentlyRetried++;
                if (retry <= currentlyRetried) break;
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        long duration = Math.round(player.getTotalDuration().toMillis());
        if (duration > 0) {
            return duration;
        }

        try {
            return AudioFileIO.read(new File(getPath())).getAudioHeader().getAudioDataLength();
        } catch (CannotReadException | IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
