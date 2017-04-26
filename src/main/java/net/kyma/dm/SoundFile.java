package net.kyma.dm;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
