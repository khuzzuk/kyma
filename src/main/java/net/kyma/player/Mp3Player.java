package net.kyma.player;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;

import java.io.IOException;

@Log4j2
@Getter(AccessLevel.PACKAGE)
abstract class Mp3Player {
    private Mp3File metadata;
    @Setter(AccessLevel.PACKAGE)
    private long startedTime;
    private String path;
    private long stoppedAt;

    Mp3Player(SoundFile file) {
        this.path = file.getPath();
    }

    void initMetadata() {
        try {
            metadata = new Mp3File(path);
        } catch (IOException e) {
            log.error("File read problems: " + path);
            log.error(e.getStackTrace());
        } catch (UnsupportedTagException e) {
            log.error("Mp3 file has unsupported tag set");
        } catch (InvalidDataException e) {
            log.error("Mp3 file is invalid");
        }
    }

    long getLength() {
        return metadata.getLengthInMilliseconds();
    }

    long playbackStatus() {
        return System.currentTimeMillis() - startedTime;
    }
}
