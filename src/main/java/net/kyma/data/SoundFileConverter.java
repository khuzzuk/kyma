package net.kyma.data;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import org.apache.lucene.document.Document;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.kyma.dm.MetadataField.*;

@Log4j2
@Singleton
public class SoundFileConverter {
    @Inject
    public SoundFileConverter(Bus bus, @Named("messages") Properties messages) {
        bus.<File, SoundFile>setResponse(messages.getProperty("playlist.add.file"), f -> from(f, f.getParent()));
        bus.<Collection<Document>>setReaction(messages.getProperty("data.convert.from.doc.gui"),
                docs -> bus.send(messages.getProperty("data.view.refresh"),
                        docs.stream().map(this::from).collect(Collectors.toList())));
    }

    public SoundFile from(File file, String indexedPath) {
        SoundFile sound = new SoundFile();
        sound.setPath(file.getPath());
        sound.setFileName(file.getName());
        sound.setIndexedPath(file.getPath().replace(indexedPath, ""));

        Optional.ofNullable(getMetadataFrom(file)).ifPresent(m -> fillData(sound, m));
        return sound;
    }

    private SoundFile from(Document document) {
        SoundFile soundFile = new SoundFile();
        soundFile.setPath(document.get(PATH.getName()));
        soundFile.setIndexedPath(document.get(INDEXED_PATH.getName()));
        soundFile.setFileName(document.get(FILE_NAME.getName()));
        soundFile.setTitle(document.get(TITLE.getName()));
        soundFile.setRate(document.getField(RATE.getName()).numericValue().intValue());
        soundFile.setYear(document.getField(YEAR.getName()).numericValue().intValue());
        soundFile.setAlbum(document.get(ALBUM.getName()));

        return soundFile;
    }

    private void fillData(SoundFile sound, Tag metadata) {
        sound.setTitle(metadata.getFirst(FieldKey.TITLE));
        fillNumericField(FieldKey.RATING, metadata, sound::setRate);
        fillNumericField(FieldKey.YEAR, metadata, sound::setYear);
        sound.setAlbum(metadata.getFirst(FieldKey.ALBUM));
    }

    private void fillNumericField(FieldKey fieldKey, Tag tags, Consumer<Integer> consumer) {
        String s = tags.getFirst(fieldKey);
        if (s.length() > 0 && s.length() == s.replace("^[0-9]", "").length()) {
            consumer.accept(Integer.parseInt(s));
        }
    }

    private Tag getMetadataFrom(File file) {
        try {
            return AudioFileIO.read(file).getTag();
        } catch (CannotReadException |
                IOException |
                TagException |
                ReadOnlyFileException |
                InvalidAudioFrameException e) {
            log.error(e);
        }
        return null;
    }
}
