package net.kyma.data;

import static net.kyma.EventType.FILES_EXECUTE;
import static net.kyma.EventType.SHOW_ALERT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.disk.FileOperation;
import net.kyma.dm.SoundFile;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@AllArgsConstructor
public class MetadataIndexer implements Loadable {
    private Bus<EventType> bus;

    @Override
    public void load() {
        bus.subscribingFor(EventType.DATA_STORE_ITEM).accept(this::asyncIndexSingle).subscribe();
        bus.subscribingFor(EventType.DATA_STORE_LIST).accept(this::indexList).subscribe();
    }

    private void indexList(Collection<SoundFile> soundFiles) {
        soundFiles.forEach(this::asyncIndexSingle);
    }

    private void asyncIndexSingle(SoundFile soundFile) {
        bus.message(FILES_EXECUTE).withContent(new FileOperation(Paths.get(soundFile.getPath()), () -> index(soundFile))).send();
    }

    private void index(SoundFile soundFile) {
        try {
            AudioFile file = AudioFileIO.read(new File(soundFile.getPath()));
            MetadataConverter.updateMetadata(file.getTag(), soundFile);
            file.commit();
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            //TODO if InvalidAudioFrameException, ask user if file should be removed from index, or deleted
            bus.message(SHOW_ALERT).withContent(String.format("cannot read from file: %s\n%s", soundFile.getPath(), e)).send();
        } catch (CannotWriteException e) {
            bus.message(SHOW_ALERT).withContent(String.format("cannot write to file: %s\n%s", soundFile.getPath(), e)).send();
        }
    }
}
