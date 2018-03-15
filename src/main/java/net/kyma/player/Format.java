package net.kyma.player;

import java.util.EnumSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@AllArgsConstructor
@Getter
public enum Format {
    MP3(Mp3PlayerJLayer::new, true),
    M4A(M4aPlayerFX::new, true),
    FLAC(FLACPlayer::new, false),
    UNKNOWN(null, false);

    final PlayerSupplier playerSupplier;
    private static final Set<Format> SET = EnumSet.allOf(Format.class);
    public static final Set<String> supportedFormats = Set.of(".mp3", ".flac", ".m4a");
    private boolean byteScale;

    public static Format forPath(String path) {
        return SET.stream().filter(f -> path.toUpperCase().endsWith(f.name())).findAny().orElse(UNKNOWN);
    }

    public Player getPlayer(SoundFile file, Bus bus) {
        if (playerSupplier != null) return playerSupplier.getPlayer(file, bus);
        return null;
    }

    interface PlayerSupplier {
        Player getPlayer(SoundFile file, Bus bus);
    }
}
