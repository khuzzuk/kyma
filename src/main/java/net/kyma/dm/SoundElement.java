package net.kyma.dm;

import lombok.*;

@Data
public class SoundElement extends BaseElement {
    private SoundFile soundFile;

    public SoundElement(SoundFile soundFile) {
        super();
        this.soundFile = soundFile;
        setName(soundFile.getFileName());
    }
}
