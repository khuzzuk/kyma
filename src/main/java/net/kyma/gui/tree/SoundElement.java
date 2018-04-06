package net.kyma.gui.tree;

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

    @Override
    public boolean isBranch()
    {
        return false;
    }
}
