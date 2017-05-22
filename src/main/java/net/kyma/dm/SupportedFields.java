package net.kyma.dm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.kyma.player.Format;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;

@Getter
@AllArgsConstructor
@Log4j2
public enum SupportedFields {
    PATH("path", null, SoundFile::getPath, SoundFile::setPath, null),
    INDEXED_PATH("indexedPath", null, SoundFile::getIndexedPath, SoundFile::setIndexedPath, null),
    FORMAT("format", null, s -> s.getFormat().name(), (s, v) -> s.setFormat(Format.forPath(s.getPath())), null),
    FILE_NAME("fileName", null, SoundFile::getFileName, SoundFile::setFileName, null),
    // Not in Supported Tags, but implements tag setter
    RATE("rate", FieldKey.RATING, s -> s.getRate() != null ? s.getRate().name() : Rating.UNDEFINED.name(), (s, v) -> {
        if (isNoneBlank(v)) {
            s.setRate(Rating.valueOf(v));
        } else {
            s.setRate(Rating.UNDEFINED);
        }
    }, null) {
        @Override
        public void setField(Tag tag, SoundFile soundFile) {
            tag.deleteField(getMappedKey());
            try {
                tag.setField(getMappedKey(), String.valueOf(soundFile.getRateValue()));
            } catch (FieldDataInvalidException e) {
                log.error("Cannot set Field " + getMappedKey().name() + " with value " + soundFile.getRateValue());
                log.error(e);
            }
        }
    },
    TITLE("title", FieldKey.TITLE, SoundFile::getTitle, SoundFile::setTitle, null),
    YEAR("year", FieldKey.YEAR, SoundFile::getDate, SoundFile::setDate, StringUtils::isNoneBlank),
    ALBUM("album", FieldKey.ALBUM, SoundFile::getAlbum, SoundFile::setAlbum, null),
    ALBUM_ARTIST("albumArtist", FieldKey.ALBUM_ARTIST, SoundFile::getAlbumArtist, SoundFile::setAlbumArtist, null),
    ALBUM_ARTISTS("albumArtists", FieldKey.ALBUM_ARTISTS, SoundFile::getAlbumArtists, SoundFile::setAlbumArtists, null),
    ARTIST("artist", FieldKey.ARTIST, SoundFile::getArtist, SoundFile::setArtist, null),
    ARTISTS("artists", FieldKey.ARTISTS, SoundFile::getArtists, SoundFile::setArtists, null),
    COMPOSER("composer", FieldKey.COMPOSER, SoundFile::getComposer, SoundFile::setComposer, null),
    CONDUCTOR("conductor", FieldKey.CONDUCTOR, SoundFile::getConductor, SoundFile::setConductor, null),
    COUNTRY("country", FieldKey.COUNTRY, SoundFile::getCountry, SoundFile::setCountry, null),
    CUSTOM1("custom1", FieldKey.CUSTOM1, SoundFile::getCustom1, SoundFile::setCustom1, null),
    CUSTOM2("custom2", FieldKey.CUSTOM2, SoundFile::getCustom2, SoundFile::setCustom2, null),
    CUSTOM3("custom3", FieldKey.CUSTOM3, SoundFile::getCustom3, SoundFile::setCustom3, null),
    CUSTOM4("custom4", FieldKey.CUSTOM4, SoundFile::getCustom4, SoundFile::setCustom4, null),
    CUSTOM5("custom5", FieldKey.CUSTOM5, SoundFile::getCustom5, SoundFile::setCustom5, null),
    DISC_NO("discNo", FieldKey.DISC_NO, SoundFile::getDiscNo, SoundFile::setDiscNo, NumberUtils::isDigits),
    GENRE("genre", FieldKey.GENRE, SoundFile::getGenre, SoundFile::setGenre, null),
    GROUP("group", FieldKey.GROUP, SoundFile::getGroup, SoundFile::setGroup, null),
    INSTRUMENT("instrument", FieldKey.INSTRUMENT, SoundFile::getInstrument, SoundFile::setInstrument, null),
    MOOD("mood", FieldKey.MOOD, SoundFile::getMood, SoundFile::setMood, null),
    MOVEMENT("movement", FieldKey.MOVEMENT, SoundFile::getMovement, SoundFile::setMovement, null),
    OCCASION("occasion", FieldKey.OCCASION, SoundFile::getOccasion, SoundFile::setOccasion, null),
    OPUS("opus", FieldKey.OPUS, SoundFile::getOpus, SoundFile::setOpus, null),
    ORCHESTRA("orchestra", FieldKey.ORCHESTRA, SoundFile::getOrchestra, SoundFile::setOrchestra, null),
    QUALITY("quality", FieldKey.QUALITY, SoundFile::getQuality, SoundFile::setQuality, null),
    RANKING("ranking", FieldKey.RANKING, SoundFile::getRanking, SoundFile::setRanking, null),
    TEMPO("tempo", FieldKey.TEMPO, SoundFile::getTempo, SoundFile::setTempo, null),
    TONALITY("tonality", FieldKey.TONALITY, SoundFile::getTonality, SoundFile::setTonality, null),
    TRACK("track", FieldKey.TRACK, SoundFile::getTrack, SoundFile::setTrack, null),
    WORK("work", FieldKey.WORK, SoundFile::getWork, SoundFile::setWork, null),
    WORK_TYPE("workType", FieldKey.WORK_TYPE, SoundFile::getWorkType, SoundFile::setWorkType, null),
    COUNTER("playCounter", null, s -> String.valueOf(s.getCounter()),
            (s, v) -> s.setCounter(NumberUtils.toInt(v)), null);

    @NonNull
    private final String name;
    private final FieldKey mappedKey;
    @NonNull
    private final Function<SoundFile, String> getter;
    @NonNull
    private final BiConsumer<SoundFile, String> setter;
    private final Predicate<String> validator;

    public static final Set<SupportedFields> SET = EnumSet.allOf(SupportedFields.class);
    public static final Set<SupportedFields> SUPPORTED_TAG = EnumSet.of(
            TITLE, YEAR, ALBUM, ALBUM_ARTIST, ALBUM_ARTISTS, ARTIST, ARTISTS, COMPOSER, CONDUCTOR, COUNTRY,
            CUSTOM1, CUSTOM2, CUSTOM3, CUSTOM4, CUSTOM5, DISC_NO, GENRE, GROUP, INSTRUMENT, MOOD, MOVEMENT,
            OCCASION, OPUS, ORCHESTRA, QUALITY, RANKING, TEMPO, TONALITY, TRACK, WORK, WORK_TYPE);

    public void setField(Tag tag, SoundFile soundFile) {
        String value = getter.apply(soundFile);
        try {
            if (validator == null || validator.test(value)) {
                tag.setField(mappedKey, value);
            }
        } catch (FieldDataInvalidException e) {
            log.error("Cannot set Field " + mappedKey.name() + " with value " + value);
            log.error(e);
        }
    }
}
