package net.kyma.gui;

import lombok.*;
import net.kyma.dm.SoundFile;

@EqualsAndHashCode(callSuper = true)
@Data
public class SoundElement extends BaseElement {
    private SoundFile soundFile;

    public SoundElement(SoundFile soundFile) {
        super();
        this.soundFile = soundFile;
        setName(soundFile.getFileName());
    }

}
