package net.kyma.data;

import com.google.inject.Inject;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;

@Singleton
@Log4j2
public class FileCleaner {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;

    public void init() {
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("file.remove"), c -> c.forEach(this::removeFile));
    }

    private void removeFile(SoundFile soundFile) {
        try {
            Files.delete(Paths.get(soundFile.getPath()));
        } catch (IOException e) {
            log.error("cannot remove file" + soundFile.getPath());
            log.error(e);
        }
    }
}
