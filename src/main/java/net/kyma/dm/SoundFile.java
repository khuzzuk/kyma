package net.kyma.dm;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Log4j2
@Getter
@Setter
public class SoundFile implements Comparable<SoundFile> {
    private String path;
    private String fileName;
    private String indexedPath;
    private String title;

    @Override
    public int compareTo(SoundFile o) {
        return getPath().compareTo(o.getPath());
    }
}
