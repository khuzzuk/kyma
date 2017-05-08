package net.kyma.data;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

import static org.jaudiotagger.tag.FieldKey.*;

@Log4j2
class MetadataConverter {
    static Tag tagFrom(SoundFile soundFile) {
        return getMetadataFrom(new File(soundFile.getPath()));
    }

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
        metadata.setField(TITLE, updateSource.getTitle());
        metadata.setField(RATING, String.valueOf(updateSource.getRate()));
        metadata.setField(YEAR, updateSource.getDate());
        metadata.setField(ALBUM, updateSource.getAlbum());
        metadata.setField(ALBUM_ARTIST, updateSource.getAlbumArtist());
        metadata.setField(ARTIST, updateSource.getArtist());
        metadata.setField(COMPOSER, updateSource.getComposer());
        metadata.setField(CONDUCTOR, updateSource.getConductor());
        metadata.setField(COUNTRY, updateSource.getCountry());
        metadata.setField(CUSTOM1, updateSource.getCustom1());
        metadata.setField(CUSTOM2, updateSource.getCustom2());
        metadata.setField(CUSTOM3, updateSource.getCustom3());
        metadata.setField(CUSTOM4, updateSource.getCustom4());
        metadata.setField(CUSTOM5, updateSource.getCustom5());
        metadata.setField(DISC_NO, updateSource.getDiscNo());
        metadata.setField(GENRE, updateSource.getGenre());
        metadata.setField(GROUP, updateSource.getGroup());
        metadata.setField(INSTRUMENT, updateSource.getInstrument());
        metadata.setField(MOOD, updateSource.getMood());
        metadata.setField(MOVEMENT, updateSource.getMovement());
        metadata.setField(OCCASION, updateSource.getOccasion());
        metadata.setField(OPUS, updateSource.getOpus());
        metadata.setField(ORCHESTRA, updateSource.getOrchestra());
        metadata.setField(QUALITY, updateSource.getQuality());
        metadata.setField(RANKING, updateSource.getRanking());
        metadata.setField(TEMPO, updateSource.getTempo());
        metadata.setField(TONALITY, updateSource.getTonality());
        metadata.setField(TRACK, updateSource.getTrack());
        metadata.setField(WORK, updateSource.getWork());
        metadata.setField(WORK_TYPE, updateSource.getWorkType());
    }
}
