package net.kyma.dm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoundFile implements Comparable<SoundFile> {
    private String path;
    private String fileName;
    private String indexedPath;
    private String title;
    private int rate;

    @Override
    public int compareTo(SoundFile o) {
        return getPath().compareTo(o.getPath());
    }
}
