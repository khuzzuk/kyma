package net.kyma.dm;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Entity
@Indexed
@Log4j2
public class SoundFile {
    @Id
    @Getter
    @Setter
    private String path;
    @Field(analyze = Analyze.NO)
    @Getter
    @Setter
    private String title;
    public static SoundFile from(Mp3File metadata) {
        SoundFile file = new SoundFile();
        file.path = metadata.getFilename();
        fillData(file, metadata);
        return file;
    }

    public static SoundFile from(File file) {
        SoundFile sound = new SoundFile();
        Mp3File metadata = getMetadataFrom(file);
        sound.path = file.getPath();
        if (metadata == null) {
            return sound;
        }
        fillData(sound, metadata);
        return sound;
    }

    private static void fillData(SoundFile sound, Mp3File metadata) {
        sound.title = Optional.ofNullable(metadata.getId3v2Tag().getTitle()).orElse(metadata.getId3v1Tag().getTitle());
    }

    private static Mp3File getMetadataFrom(File file) {
        try {
            return new Mp3File(file);
        } catch (IOException e) {
            log.error("file is not accessible");
            log.error(e);
        } catch (UnsupportedTagException e) {
            log.error("file has unsupported set of tags");
            log.error(e);
        } catch (InvalidDataException e) {
            log.error("File is corrupted");
            log.error(e);
        }
        return null;
    }
}
