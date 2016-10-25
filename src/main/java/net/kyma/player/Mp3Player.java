package net.kyma.player;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Log4j2
public class Mp3Player {
    private String path;
    private AdvancedPlayer player;
    private Mp3File metadata;
    private long startedTime;

    public Mp3Player(String path) {
        this.path = path;
    }

    void start() {
        if (player == null) {
            try {
                player = new AdvancedPlayer(new FileInputStream(new File(path)));
                startedTime = System.currentTimeMillis();
                player.play();
            } catch (JavaLayerException e) {
                log.error("Could not read stream, expected mp3 audio file");
                log.error(e.getStackTrace());
            } catch (FileNotFoundException e) {
                log.error("No file in " + path);
                log.error(e.getStackTrace());
            }
        }
    }

    long getLength() {
        return metadata.getLengthInMilliseconds();
    }

    void stop() {
        player.stop();
    }

    public void init() {
        try {
            metadata = new Mp3File(path);
        } catch (IOException e) {
            log.error("File read problems: " + path);
            log.error(e.getStackTrace());
        } catch (UnsupportedTagException e) {
            log.error("Mp3 file has unsopported tag set");
        } catch (InvalidDataException e) {
            log.error("Mp3 file is invalid");
        }

    }

    public long playbackStatus() {
        return System.currentTimeMillis() - startedTime;
    }
}
