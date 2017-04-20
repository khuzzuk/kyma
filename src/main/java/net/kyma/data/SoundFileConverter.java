package net.kyma.data;

import com.beaglebuddy.mp3.MP3;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.MetadataField;
import net.kyma.dm.SoundFile;
import org.apache.lucene.document.Document;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static net.kyma.dm.MetadataField.*;

@Log4j2
@Singleton
public class SoundFileConverter {
    private Bus bus;
    private Properties messages;

    @Inject
    public SoundFileConverter(Bus bus, @Named("messages") Properties messages) {
        this.bus = bus;
        this.messages = messages;
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

        MP3 metadata = getMetadataFrom(file);
        if (metadata == null) {
            return sound;
        }

        fillData(sound, metadata);
        return sound;
    }

    private SoundFile from(Document document) {
        SoundFile soundFile = new SoundFile();
        soundFile.setPath(document.get(PATH.getName()));
        soundFile.setIndexedPath(document.get(INDEXED_PATH.getName()));
        soundFile.setFileName(document.get(FILE_NAME.getName()));
        soundFile.setTitle(document.get(TITLE.getName()));
        soundFile.setRate(document.getField(RATE.getName()).numericValue().intValue());
        return soundFile;
    }

    private void fillData(SoundFile sound, MP3 metadata) {
        sound.setTitle(metadata.getTitle());
        sound.setRate(metadata.getRating());
    }

    private MP3 getMetadataFrom(File file) {
        try {
            return new MP3(file.getPath());
        } catch (IOException e) {
            log.error("file is not accessible");
            log.error(e);
        }
        return null;
    }

    private Collection<SoundFile> convert(Collection<Document> documents) {
        return documents.stream().map(this::from).collect(Collectors.toList());
    }
}
