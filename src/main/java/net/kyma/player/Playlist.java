package net.kyma.player;

import static net.kyma.EventType.FILES_REMOVE;
import static net.kyma.EventType.PLAYER_PLAY;
import static net.kyma.EventType.PLAYER_STOP;
import static net.kyma.EventType.PLAYLIST_ADD_LIST;
import static net.kyma.EventType.PLAYLIST_NEXT;
import static net.kyma.EventType.PLAYLIST_PREVIOUS;
import static net.kyma.EventType.PLAYLIST_REFRESH;
import static net.kyma.EventType.PLAYLIST_REMOVE_LIST;
import static net.kyma.EventType.PLAYLIST_REMOVE_SOUND;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
@Log4j2
public class Playlist implements Loadable {
    private final Bus<EventType> bus;
    private List<SoundFile> fileList;
    private SoundFile currentlyPlayed;
    private int index = -1;

    @Override
    public void load() {
        fileList = new LinkedList<>();
        bus.subscribingFor(PLAYLIST_ADD_LIST).accept(this::addAll).subscribe();
        bus.subscribingFor(PLAYLIST_REMOVE_LIST).accept(this::removeAll).subscribe();
        bus.subscribingFor(PLAYLIST_NEXT).then(this::playNextItem).subscribe();
        bus.subscribingFor(PLAYLIST_PREVIOUS).then(this::playPreviousItem).subscribe();
        bus.subscribingFor(PLAYLIST_REMOVE_SOUND).accept(this::fileRemoved).subscribe();
    }

    private synchronized void removeAll(List<PlaylistEvent> toRemove) {
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            PlaylistEvent removeEvent = toRemove.get(i);
            if (i <= index) {
                index--;
            }
            SoundFile removed = fileList.remove(removeEvent.getPosition());
            if (!removed.equals(removeEvent.getFile())) {
                log.error("removed file does not match");
            }
        }
        boolean isCurrentlyPlayedRemoved = toRemove.stream()
              .anyMatch(removed -> removed.getFile().equals(currentlyPlayed));
        sendRefreshEvent(isCurrentlyPlayedRemoved ? -1 : index);
    }

    private synchronized void addAll(Collection<SoundFile> soundFiles) {
        fileList.addAll(soundFiles);
        sendRefreshEvent(index);
    }

    private synchronized void playNextItem() {
        if (fileList.isEmpty()) return;
        if (++index == fileList.size()) index = 0;

        currentlyPlayed = fileList.get(index);
        bus.message(PLAYER_PLAY).withContent(currentlyPlayed).send();
        sendRefreshEvent(index);
    }

    private synchronized void playPreviousItem() {
        if (fileList.isEmpty()) return;
        if (--index < 0) index = fileList.size() - 1;

        bus.message(PLAYER_PLAY).withContent(fileList.get(index)).send();
        sendRefreshEvent(index);
    }

    private void fileRemoved(List<SoundFile> toRemove) {
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            SoundFile fileToRemove = toRemove.get(i);
            if (fileList.contains(fileToRemove)) {
                if (index == fileList.indexOf(fileToRemove)) index--;
                fileList.remove(fileToRemove);

                if (fileList.contains(fileToRemove)) i++; //NOSONAR removing file requires to stop playing it at all cost
            }
        }

        if (toRemove.contains(currentlyPlayed)) {
            bus.message(PLAYER_STOP).withResponse(PLAYLIST_NEXT).send();
        }

        sendRefreshEvent(index);
        bus.message(FILES_REMOVE).withContent(toRemove).send();
    }

    private void sendRefreshEvent(int index) {
        bus.message(PLAYLIST_REFRESH)
              .withContent(PlaylistRefreshEvent.builder()
              .playlist(Collections.unmodifiableList(fileList))
              .position(index)
              .build()).send();
    }
}
