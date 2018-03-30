package net.kyma.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
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
        bus.subscribingFor(EventType.DATA_STORE_ITEM).<SoundFile>accept(this::index).subscribe();
        bus.subscribingFor(EventType.DATA_STORE_LIST).<Collection<SoundFile>>accept(this::index).subscribe();
    }

    private void index(Collection<SoundFile> soundFiles) {
        soundFiles.forEach(this::index);
    }

    private void index(SoundFile soundFile) {
        try {
            AudioFile file = AudioFileIO.read(new File(soundFile.getPath()));
            MetadataConverter.updateMetadata(file.getTag(), soundFile);
            file.commit();
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            //TODO if InvalidAudioFrameException, ask user if file should be removed from index, or deleted
            log.error(" cannot read from file: " + soundFile.getPath());
            log.error(e);
        } catch (CannotWriteException e) {
            log.error(" cannot write to file: " + soundFile.getPath());
            log.error(e);
        }
    }
}
