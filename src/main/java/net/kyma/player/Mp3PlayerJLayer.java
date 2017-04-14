package net.kyma.player;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Log4j2
public class Mp3PlayerJLayer extends Mp3Player {
    private AdvancedPlayer player;

    public Mp3PlayerJLayer(SoundFile file) {
        super(file);
    }

    void start() {
        if (player == null) {
            try {
                player = new AdvancedPlayer(new FileInputStream(new File(getPath())));
                setStartedTime(System.currentTimeMillis());
                player.play();
            } catch (JavaLayerException e) {
                log.error("Could not read stream, expected mp3 audio file");
                log.error(e.getStackTrace());
            } catch (FileNotFoundException e) {
                log.error("No file in " + getPath());
                log.error(e.getStackTrace());
            }
        }
    }

    void stop() {
        player.close();
        player = null;
    }
}
