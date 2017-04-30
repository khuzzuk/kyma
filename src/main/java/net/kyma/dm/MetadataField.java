package net.kyma.dm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MetadataField {
    TITLE("title"),
    PATH("path"),
    INDEXED_PATH("indexedPath"),
    FILE_NAME("fileName"),
    RATE("rate"),
    YEAR("year"),
    ALBUM("album"),
    ALBUM_ARTIST("albumArtist"),
    ALBUM_ARTISTS("albumArtists"),
    ARTIST("artist"),
    ARTISTS("artists"),
    COMPOSER("composer"),
    CONDUCTOR("conductor"),
    COUNTRY("country"),
    CUSTOM1("custom1"),
    CUSTOM2("custom2"),
    CUSTOM3("custom3"),
    CUSTOM4("custom4"),
    CUSTOM5("custom5"),
    DISC_NO("discNo"),
    GENRE("genre"),
    GROUP("group"),
    INSTRUMENT("instrument"),
    MOOD("mood"),
    MOVEMENT("movement"),
    OCCASION("occasion"),
    OPUS("opus"),
    ORCHESTRA("orchestra"),
    QUALITY("quality"),
    RANKING("ranking"),
    TEMPO("tempo"),
    TONALITY("tonality"),
    TRACK("track"),
    WORK("work"),
    WORK_TYPE("workType");

    private final String name;
}
