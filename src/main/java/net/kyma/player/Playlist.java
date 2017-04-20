package net.kyma.player;

import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Singleton
public class Playlist {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    private List<SoundFile> playlist;
    private int currentPos;

    public void init() {
        playlist = new ArrayList<>();
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("playlist.add.list"), playlist::addAll);
        bus.setReaction(messages.getProperty("playlist.start"), this::playFirstItem);
        bus.setReaction(messages.getProperty("playlist.next"), this::playNextItem);
    }

    private void playFirstItem() {
        currentPos = 0;
        playNextItem();
    }
    private void playNextItem() {
        if (playlist.size() == 0) return;
        if (currentPos >= playlist.size()) currentPos = 0;
        bus.send(messages.getProperty("player.play.mp3"), playlist.get(currentPos));
        bus.send(messages.getProperty("playlist.highlight"), currentPos);
        currentPos++;
    }
}
