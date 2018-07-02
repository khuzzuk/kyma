package net.kyma.disk;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyma.dm.SoundFile;

@AllArgsConstructor
@Getter(AccessLevel.PACKAGE)
public class FileOperation {
   private SoundFile soundFile;
   private Runnable operation;
}
