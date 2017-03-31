package net.kyma.dm;

import com.mpatric.mp3agic.Mp3File;

public class SoundFile {
    static SoundFile fromMetadata(Mp3File metadata) {
        SoundFile file = new SoundFile();
        metadata.getId3v1Tag().getTitle();
    }
}
