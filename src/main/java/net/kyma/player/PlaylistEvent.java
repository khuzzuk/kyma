package net.kyma.player;

import lombok.Data;
import net.kyma.dm.SoundFile;

@Data
public class PlaylistEvent {
   private final SoundFile file;
   private final int position;
}
