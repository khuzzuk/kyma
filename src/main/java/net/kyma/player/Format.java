package net.kyma.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

@AllArgsConstructor
@Getter
public enum Format {
    MP3(Mp3PlayerJLayer::new, true),
    M4A(M4aPlayerSPI::new, false),
    FLAC(FLACPlayer::new, false),
    OGG(OggPlayerSPI::new, false),
    UNKNOWN(null, false);

    final PlayerSupplier playerSupplier;
    private static final Set<Format> SET = EnumSet.allOf(Format.class);
    private static final Set<String> supportedFormats = Set.of(".mp3", ".flac", ".m4a", ".ogg");
    private boolean byteScale;

    public static Format forPath(String path) {
        return SET.stream().filter(f -> path.toUpperCase().endsWith(f.name())).findAny().orElse(UNKNOWN);
    }

    public Player getPlayer(SoundFile file, Bus<EventType> bus) {
        if (playerSupplier != null) return playerSupplier.getPlayer(file, bus);
        return null;
    }

    interface PlayerSupplier {
        Player getPlayer(SoundFile file, Bus<EventType> bus);
    }

    public static boolean isSupportingFormat(String extension) {
        return supportedFormats.contains(extension);
    }

    public static boolean isSupportingFormat(Path path) {
        String filename = path.getFileName().toString();
        if (Files.isRegularFile(path) && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase();
            return supportedFormats.contains(ext);
        }
        return false;
    }
}
