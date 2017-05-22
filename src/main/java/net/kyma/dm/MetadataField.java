package net.kyma.dm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyma.player.Format;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum MetadataField {
    PATH("path", SoundFile::getPath, SoundFile::setPath),
    INDEXED_PATH("indexedPath", SoundFile::getIndexedPath, SoundFile::setIndexedPath),
    FORMAT("format", s -> s.getFormat().name(), (s, v) -> s.setFormat(Format.forPath(s.getPath()))),
    FILE_NAME("fileName", SoundFile::getFileName, SoundFile::setFileName),
    RATE("rate", s -> s.getRate().name(),
            (s, v) -> s.setRate(Rating.valueOf(v))),
    TITLE("title", SoundFile::getTitle, SoundFile::setTitle),
    YEAR("year", SoundFile::getDate, SoundFile::setDate),
    ALBUM("album", SoundFile::getAlbum, SoundFile::setAlbum),
    ALBUM_ARTIST("albumArtist", SoundFile::getAlbumArtist, SoundFile::setAlbumArtist),
    ALBUM_ARTISTS("albumArtists", SoundFile::getAlbumArtists, SoundFile::setAlbumArtists),
    ARTIST("artist", SoundFile::getArtist, SoundFile::setArtist),
    ARTISTS("artists", SoundFile::getArtists, SoundFile::setArtists),
    COMPOSER("composer", SoundFile::getComposer, SoundFile::setComposer),
    CONDUCTOR("conductor", SoundFile::getConductor, SoundFile::setConductor),
    COUNTRY("country", SoundFile::getCountry, SoundFile::setCountry),
    CUSTOM1("custom1", SoundFile::getCustom1, SoundFile::setCustom1),
    CUSTOM2("custom2", SoundFile::getCustom2, SoundFile::setCustom2),
    CUSTOM3("custom3", SoundFile::getCustom3, SoundFile::setCustom3),
    CUSTOM4("custom4", SoundFile::getCustom4, SoundFile::setCustom4),
    CUSTOM5("custom5", SoundFile::getCustom5, SoundFile::setCustom5),
    DISC_NO("discNo", SoundFile::getDiscNo, SoundFile::setDiscNo),
    GENRE("genre", SoundFile::getGenre, SoundFile::setGenre),
    GROUP("group", SoundFile::getGroup, SoundFile::setGroup),
    INSTRUMENT("instrument", SoundFile::getInstrument, SoundFile::setInstrument),
    MOOD("mood", SoundFile::getMood, SoundFile::setMood),
    MOVEMENT("movement", SoundFile::getMovement, SoundFile::setMovement),
    OCCASION("occasion", SoundFile::getOccasion, SoundFile::setOccasion),
    OPUS("opus", SoundFile::getOpus, SoundFile::setOpus),
    ORCHESTRA("orchestra", SoundFile::getOrchestra, SoundFile::setOrchestra),
    QUALITY("quality", SoundFile::getQuality, SoundFile::setQuality),
    RANKING("ranking", SoundFile::getRanking, SoundFile::setRanking),
    TEMPO("tempo", SoundFile::getTempo, SoundFile::setTempo),
    TONALITY("tonality", SoundFile::getTonality, SoundFile::setTonality),
    TRACK("track", SoundFile::getTrack, SoundFile::setTrack),
    WORK("work", SoundFile::getWork, SoundFile::setWork),
    WORK_TYPE("workType", SoundFile::getWorkType, SoundFile::setWorkType),
    COUNTER("playCounter", s -> String.valueOf(s.getCounter()),
            (s, v) -> s.setCounter(NumberUtils.toInt(v)));

    private final String name;
    private final Function<SoundFile, String> getter;
    private final BiConsumer<SoundFile, String> setter;

    public static final Set<MetadataField> SET = EnumSet.allOf(MetadataField.class);
}
