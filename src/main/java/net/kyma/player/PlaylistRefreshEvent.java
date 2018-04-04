package net.kyma.player;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import net.kyma.dm.SoundFile;

@Getter
@Builder
public class PlaylistRefreshEvent {
   private List<SoundFile> playlist;
   private int position;
}
