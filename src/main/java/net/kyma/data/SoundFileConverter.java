package net.kyma.data;

import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import net.kyma.player.Format;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.kyma.dm.MetadataField.*;

@Log4j2
@Singleton
public class SoundFileConverter {
    private static final String[] dateParsers = new String[]{"yyyy", "yyyy-mm-dd"};
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
        sound.setFormat(Format.forPath(file.getPath()));
        sound.setFileName(file.getName());
        sound.setIndexedPath(file.getPath().replace(indexedPath, ""));

        Optional.ofNullable(getMetadataFrom(file)).ifPresent(m -> fillData(sound, m));
        return sound;
    }

    private SoundFile from(Document document) {
        SoundFile soundFile = new SoundFile();
        soundFile.setPath(document.get(PATH.getName()));
        soundFile.setFormat(Format.forPath(soundFile.getPath()));
        soundFile.setIndexedPath(document.get(INDEXED_PATH.getName()));
        soundFile.setFileName(document.get(FILE_NAME.getName()));
        soundFile.setTitle(document.get(TITLE.getName()));
        soundFile.setRate(document.getField(RATE.getName()).numericValue().intValue());
        soundFile.setYear(document.getField(YEAR.getName()).numericValue().intValue());
        soundFile.setAlbum(document.get(ALBUM.getName()));
        soundFile.setAlbumArtist(document.get(ALBUM_ARTIST.getName()));
        soundFile.setAlbumArtists(document.get(ALBUM_ARTISTS.getName()));
        soundFile.setArtist(document.get(ARTIST.getName()));
        soundFile.setArtists(document.get(ARTISTS.getName()));
        soundFile.setComposer(document.get(COMPOSER.getName()));
        soundFile.setConductor(document.get(CONDUCTOR.getName()));
        soundFile.setCountry(document.get(COUNTRY.getName()));
        soundFile.setCustom1(document.get(CUSTOM1.getName()));
        soundFile.setCustom2(document.get(CUSTOM2.getName()));
        soundFile.setCustom3(document.get(CUSTOM3.getName()));
        soundFile.setCustom4(document.get(CUSTOM4.getName()));
        soundFile.setCustom5(document.get(CUSTOM5.getName()));
        soundFile.setDiscNo(document.get(DISC_NO.getName()));
        soundFile.setGenre(document.get(GENRE.getName()));
        soundFile.setGroup(document.get(GROUP.getName()));
        soundFile.setInstrument(document.get(INSTRUMENT.getName()));
        soundFile.setMood(document.get(MOOD.getName()));
        soundFile.setMovement(document.get(MOVEMENT.getName()));
        soundFile.setOccasion(document.get(OCCASION.getName()));
        soundFile.setOpus(document.get(OPUS.getName()));
        soundFile.setOrchestra(document.get(ORCHESTRA.getName()));
        soundFile.setQuality(document.get(QUALITY.getName()));
        soundFile.setRanking(document.get(RANKING.getName()));
        soundFile.setTempo(document.get(TEMPO.getName()));
        soundFile.setTonality(document.get(TONALITY.getName()));
        soundFile.setTrack(document.get(TRACK.getName()));
        soundFile.setWork(document.get(WORK.getName()));
        soundFile.setWorkType(document.get(WORK_TYPE.getName()));

        return soundFile;
    }

    private void fillData(SoundFile sound, Tag metadata) {
        sound.setTitle(metadata.getFirst(FieldKey.TITLE));
        sound.setRate(NumberUtils.toInt(metadata.getFirst(FieldKey.RATING), 0));
        fillDateField(FieldKey.YEAR, metadata, sound::setYear);
        sound.setAlbum(metadata.getFirst(FieldKey.ALBUM));
        sound.setAlbumArtist(metadata.getFirst(FieldKey.ALBUM_ARTIST));
        sound.setAlbumArtists(metadata.getFirst(FieldKey.ALBUM_ARTISTS));
        sound.setArtist(metadata.getFirst(FieldKey.ARTIST));
        sound.setArtists(metadata.getFirst(FieldKey.ARTISTS));
        sound.setComposer(metadata.getFirst(FieldKey.COMPOSER));
        sound.setConductor(metadata.getFirst(FieldKey.CONDUCTOR));
        sound.setCountry(metadata.getFirst(FieldKey.COUNTRY));
        sound.setCustom1(metadata.getFirst(FieldKey.CUSTOM1));
        sound.setCustom2(metadata.getFirst(FieldKey.CUSTOM2));
        sound.setCustom3(metadata.getFirst(FieldKey.CUSTOM3));
        sound.setCustom4(metadata.getFirst(FieldKey.CUSTOM4));
        sound.setCustom5(metadata.getFirst(FieldKey.CUSTOM5));
        sound.setDiscNo(metadata.getFirst(FieldKey.DISC_NO));
        sound.setGenre(metadata.getFirst(FieldKey.GENRE));
        sound.setGroup(metadata.getFirst(FieldKey.GROUP));
        sound.setInstrument(metadata.getFirst(FieldKey.INSTRUMENT));
        sound.setMood(metadata.getFirst(FieldKey.MOOD));
        sound.setMovement(metadata.getFirst(FieldKey.MOVEMENT));
        sound.setOccasion(metadata.getFirst(FieldKey.OCCASION));
        sound.setOpus(metadata.getFirst(FieldKey.OPUS));
        sound.setOrchestra(metadata.getFirst(FieldKey.ORCHESTRA));
        sound.setQuality(metadata.getFirst(FieldKey.QUALITY));
        sound.setRanking(metadata.getFirst(FieldKey.RANKING));
        sound.setTempo(metadata.getFirst(FieldKey.TEMPO));
        sound.setTonality(metadata.getFirst(FieldKey.TONALITY));
        sound.setTrack(metadata.getFirst(FieldKey.TRACK));
        sound.setWork(metadata.getFirst(FieldKey.WORK));
        sound.setWorkType(metadata.getFirst(FieldKey.WORK_TYPE));
    }

    private void fillDateField(FieldKey fieldKey, Tag tags, Consumer<Integer> consumer) {
        try {
            consumer.accept(DateUtils.parseDate(tags.getFirst(fieldKey), dateParsers).getYear());
        } catch (ParseException e) {
            log.warn(e);
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
