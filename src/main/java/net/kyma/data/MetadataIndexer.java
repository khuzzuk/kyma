package net.kyma.data;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

@Singleton
@Log4j2
public class MetadataIndexer {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;

    public void init() {
        bus.<SoundFile>setReaction(messages.getProperty("data.store.item"), this::index);
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("data.store.list"), this::index);
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
            log.error(" cannot read from file: " + soundFile.getPath());
            log.error(e);
        } catch (CannotWriteException e) {
            log.error(" cannot write to file: " + soundFile.getPath());
            log.error(e);
        }
    }
}
