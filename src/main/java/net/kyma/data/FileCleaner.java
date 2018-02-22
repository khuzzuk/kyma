package net.kyma.data;

import static net.kyma.EventType.FILES_REMOVE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@AllArgsConstructor
public class FileCleaner implements Loadable {
    private Bus<EventType> bus;

    @Override
    public void load() {
        bus.<Collection<SoundFile>>setReaction(FILES_REMOVE, c -> c.forEach(this::removeFile));
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
