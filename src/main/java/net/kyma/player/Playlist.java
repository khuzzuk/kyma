package net.kyma.player;

import static net.kyma.EventType.FILES_REMOVE;
import static net.kyma.EventType.PLAYER_PLAY;
import static net.kyma.EventType.PLAYER_STOP;
import static net.kyma.EventType.PLAYLIST_ADD_LIST;
import static net.kyma.EventType.PLAYLIST_HIGHLIGHT;
import static net.kyma.EventType.PLAYLIST_NEXT;
import static net.kyma.EventType.PLAYLIST_PREVIOUS;
import static net.kyma.EventType.PLAYLIST_REMOVE_LIST;
import static net.kyma.EventType.PLAYLIST_REMOVE_SOUND;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import org.apache.commons.collections4.iterators.LoopingListIterator;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
public class Playlist implements Loadable {
    private final Bus<EventType> bus;
    private List<SoundFile> playlist;
    private LoopingListIterator<SoundFile> iterator;
    private int index = -1;

    @Override
    public void load() {
        playlist = new LinkedList<>();
        bus.subscribingFor(PLAYLIST_ADD_LIST).accept(this::addAll).subscribe();
        bus.subscribingFor(PLAYLIST_REMOVE_LIST).accept(this::removeAll).subscribe();
        bus.subscribingFor(PLAYLIST_NEXT).then(this::playNextItem).subscribe();
        bus.subscribingFor(PLAYLIST_PREVIOUS).then(this::playPreviousItem).subscribe();
        bus.subscribingFor(PLAYLIST_REMOVE_SOUND).accept(this::maybeRemove).subscribe();
    }

    private synchronized void removeAll(Collection<SoundFile> soundFiles) {
        playlist.removeAll(soundFiles);
        bus.message(PLAYLIST_HIGHLIGHT).withContent(-1).send();
        iterator = null;
    }

    private synchronized void addAll(Collection<SoundFile> soundFiles) {
        playlist.addAll(soundFiles);
        iterator = null;
    }

    private synchronized void playNextItem() {
        if (playlist.size() == 0) return;
        initIterator();

        int index;
        SoundFile next;
        do {
            index = iterator.nextIndex();
            next = iterator.next();
            if (this.index == 0 && index == 0) break; //TODO refactor this
        } while (this.index == index);
        this.index = index;

        bus.message(PLAYLIST_HIGHLIGHT).withContent(index).send();
        bus.message(PLAYER_PLAY).withContent(next).send();
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

        bus.message(PLAYLIST_HIGHLIGHT).withContent(index).send();
        bus.message(PLAYER_PLAY).withContent(previous).send();
    }

    private void initIterator() {
        if (iterator == null) {
            iterator = new LoopingListIterator<>(playlist);
        }
    }

    private void maybeRemove(Collection<SoundFile> soundFiles) {
        if (index > 0 && index < playlist.size() && soundFiles.contains(playlist.get(index))) {
            bus.message(PLAYER_STOP).send();
            bus.message(PLAYLIST_NEXT).send();
        }
        removeAll(soundFiles);
        iterator = null;
        bus.message(FILES_REMOVE).withContent(soundFiles).send();
    }
}
