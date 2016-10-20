package net.kyma.player;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Log4j2
public class Mp3Player {
    @Setter
    private String path;
    private AdvancedPlayer player;

    public Mp3Player(String path) {
        this.path = path;
    }

    void start() {
        if (player == null) {
            try {
                player = new AdvancedPlayer(new FileInputStream(new File(path)));
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

    void stop() {
        player.stop();
    }
}
