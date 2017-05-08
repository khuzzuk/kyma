package net.kyma.dm;

import lombok.Getter;
import lombok.Setter;
import net.kyma.player.Format;

@Getter
@Setter
public class SoundFile implements Comparable<SoundFile> {
    private Format format;
    private String path;
    private String fileName;
    private String indexedPath;
    private String title;
    private int rate;
    private String date;
    private String album;
    private String albumArtist;
    private String albumArtists;
    private String artist;
    private String artists;
    private String composer;
    private String conductor;
    private String country;
    private String custom1;
    private String custom2;
    private String custom3;
    private String custom4;
    private String custom5;
    private String discNo;
    private String genre;
    private String group;
    private String instrument;
    private String mood;
    private String movement;
    private String occasion;
    private String opus;
    private String orchestra;
    private String quality;
    private String ranking;
    private String tempo;
    private String tonality;
    private String track;
    private String work;
    private String workType;

    @Override
    public int compareTo(SoundFile o) {
        return getPath().compareTo(o.getPath());
    }
}
