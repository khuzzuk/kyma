package net.kyma.gui;

import lombok.*;
import net.kyma.dm.SoundFile;

@Data
public class SoundElement extends BaseElement {
    private SoundFile soundFile;

    public SoundElement(SoundFile soundFile) {
        super();
        this.soundFile = soundFile;
        setName(soundFile.getFileName());
    }

    @Override
    public boolean hasSound() {
        return soundFile != null;
    }
}
