package net.kyma.player;

import lombok.*;

@Data
public class PlaybackPoint {
    @NonNull
    private String path;
    @NonNull
    private long time;
}
