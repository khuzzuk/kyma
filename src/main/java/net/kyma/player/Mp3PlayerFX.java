package net.kyma.player;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;

import java.io.IOException;
import java.nio.file.Paths;

@Log4j2
public class Mp3PlayerFX {
    private String path;
    private MediaPlayer player;
    private Mp3File metadata;
    private long startedTime;
    private long stoppedAt;

    public Mp3PlayerFX(SoundFile file) {
        this.path = file.getPath();
    }

    void start() {
        if (player == null) {
            Media sound = new Media(Paths.get(path).toUri().toString());
            player = new MediaPlayer(sound);
        }
        MediaView mediaView = new MediaView(player);
        startedTime = System.currentTimeMillis();
        player.play();
    }

    long getLength() {
        return metadata.getLengthInMilliseconds();
    }

    void stop() {
        player.stop();
        player = null;
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

    long playbackStatus() {
        return System.currentTimeMillis() - startedTime;
    }
}
