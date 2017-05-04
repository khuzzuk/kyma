package net.kyma.player;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

@AllArgsConstructor
public enum Format {
    MP3(Mp3PlayerFX::new),
    FLAC(FLACPlayer::new),
    UNKNOWN(null);

    final PlayerSupplier playerSupplier;
    private static final Set<Format> SET = EnumSet.allOf(Format.class);
    public static final Set<String> supportedFormats = Sets.newHashSet(".mp3", ".flac");

    public static Format forPath(String path) {
        return SET.stream().filter(f -> path.toUpperCase().endsWith(f.name())).findAny().orElse(UNKNOWN);
    }

    public Player getPlayer(SoundFile file, Bus bus, Properties messages) {
        if (playerSupplier != null) return playerSupplier.getPlayer(file, bus, messages);
        return null;
    }

    interface PlayerSupplier {
        Player getPlayer(SoundFile file, Bus bus, Properties messages);
    }
}
