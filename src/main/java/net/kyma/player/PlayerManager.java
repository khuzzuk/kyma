package net.kyma.player;

import static net.kyma.EventType.PLAYER_SET_SLIDER;

import javafx.scene.control.Slider;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class PlayerManager implements Loadable
{
    private final Bus<EventType> bus;
    private PlaybackTimer timer;
    private Player currentPlayer;
    @Setter
    private Slider slider;

    @Override
    public void load() {
        bus.setReaction(PLAYER_SET_SLIDER, this::setSlider);
        bus.setReaction(EventType.PLAYER_PLAY, this::playMp3);
        bus.setReaction(EventType.PLAYER_PAUSE, this::pauseMp3);
        bus.setReaction(EventType.PLAYER_STOP, this::stopMp3);
        bus.setReaction(EventType.PLAYER_RESUME, this::resume);
        bus.setReaction(EventType.CLOSE, this::stopMp3);
        bus.setReaction(EventType.CLOSE, FLACPlayer::closeFLACPlayers);
        bus.setReaction(EventType.PLAYER_PLAY_FROM, this::startFrom);

        timer = new PlaybackTimer(bus, this);
        timer.load();
    }

    private synchronized void playMp3(SoundFile file) {
        if (currentPlayer != null) {
            if (currentPlayer.isPaused()) {
                currentPlayer.start();
                timer.start();
                return;
            } else {
                currentPlayer.stop();
            }
        }
        currentPlayer = file.getFormat().getPlayer(file, bus);
        if (currentPlayer == null) {
            //TODO send communicate to the user
            return;
        }
        log.info("sta play");
        currentPlayer.start();
        slider.setMax(currentPlayer.getLength());
        timer.start();
    }

    private synchronized void resume() {
        if (currentPlayer != null && currentPlayer.isPaused()) {
            currentPlayer.start();
            timer.start();
        } else {
            bus.send(EventType.PLAYLIST_NEXT);
        }
    }

    private synchronized void stopMp3() {
        timer.stop();
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
        }
    }

    private synchronized void pauseMp3() {
        timer.stop();
        if (currentPlayer != null) {
            currentPlayer.pause();
        }
    }

    private synchronized void startFrom(Long millis) {
        if (currentPlayer != null) {
            currentPlayer.startFrom(millis);
            slider.setMax(currentPlayer.getLength());
            timer.start();
        }
    }

    void updateSlider()
    {
        slider.setValue(currentPlayer.playbackStatus());
    }
}
