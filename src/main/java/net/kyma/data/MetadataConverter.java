package net.kyma.data;

import static org.jaudiotagger.tag.FieldKey.COMMENT;

import java.io.File;
import java.io.IOException;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import org.apache.commons.lang3.math.NumberUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

@Log4j2
@UtilityClass
class MetadataConverter {
    static Tag getMetadataFrom(File file) {
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

    static void updateMetadata(Tag metadata, SoundFile updateSource) throws FieldDataInvalidException {
        for (SupportedField field : SupportedField.SUPPORTED_TAG)
        {
            field.setField(metadata, updateSource);
        }
        SupportedField.RATE.setField(metadata, updateSource);

        String comment = metadata.getFirst(COMMENT);
        if (comment.contains(":") && NumberUtils.isDigits(comment.substring(0, comment.indexOf(':')))) {
            metadata.setField(COMMENT, updateSource.getCounter() + comment.substring(comment.indexOf(':')));
        } else {
            metadata.setField(COMMENT, updateSource.getCounter() + ":" + comment);
        }
    }
}
