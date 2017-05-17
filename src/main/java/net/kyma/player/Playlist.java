package net.kyma.player;

import net.kyma.dm.SoundFile;
import org.apache.commons.collections4.iterators.LoopingListIterator;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class Playlist {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    private List<SoundFile> playlist;
    private LoopingListIterator<SoundFile> iterator;
    private int index = -1;

    public void init() {
        playlist = new LinkedList<>();
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("playlist.add.list"), this::addAll);
        bus.<Collection<SoundFile>>setReaction(messages.getProperty("playlist.remove.list"), this::removeAll);
        bus.setReaction(messages.getProperty("playlist.next"), this::playNextItem);
        bus.setReaction(messages.getProperty("playlist.previous"), this::playPreviousItem);
    }

    private synchronized void removeAll(Collection<SoundFile> soundFiles) {
        playlist.removeAll(soundFiles);
        bus.send(messages.getProperty("playlist.highlight"), -1);
        iterator = null;
        initIterator();
    }

    private synchronized void addAll(Collection<SoundFile> soundFiles) {
        playlist.addAll(soundFiles);
        iterator = null;
        initIterator();
    }

    private synchronized void playNextItem() {
        if (playlist.size() == 0) return;
        initIterator();

        int index;
        SoundFile next;
        do {
            index = iterator.nextIndex();
            next = iterator.next();
        } while (this.index == index);
        this.index = index;

        bus.send(messages.getProperty("playlist.highlight"), index);
        bus.send(messages.getProperty("player.play.mp3"), next);
    }

    private synchronized void playPreviousItem() {
        if (playlist.size() == 0) return;
        initIterator();

        int index;
        SoundFile previous;
        do {
            index = iterator.previousIndex();
            previous = iterator.previous();
        } while (this.index == index);
        this.index = index;

        bus.send(messages.getProperty("playlist.highlight"), index);
        bus.send(messages.getProperty("player.play.mp3"), previous);
    }

    private void initIterator() {
        if (iterator == null) {
            iterator = new LoopingListIterator<>(playlist);
        }
    }
}
