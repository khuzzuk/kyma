package net.kyma.player;

import com.beaglebuddy.mp3.MP3;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;

import java.io.IOException;

@Log4j2
@Getter(AccessLevel.PACKAGE)
abstract class Mp3Player {
    private MP3 metadata;
    @Setter(AccessLevel.PACKAGE)
    private long startedTime;
    private String path;
    private long stoppedAt;

    Mp3Player(SoundFile file) {
        this.path = file.getPath();
    }

    void initMetadata() {
        try {
            metadata = new MP3(path);
        } catch (IOException e) {
            log.error("File read problems: " + path);
            log.error(e.getStackTrace());
        }
    }

    abstract long getLength();

    long playbackStatus() {
        return System.currentTimeMillis() - startedTime;
    }
}
