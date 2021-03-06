package net.kyma.gui.player;

import static java.lang.Math.round;
import static net.kyma.EventType.GUI_VOLUME_GET;
import static net.kyma.EventType.GUI_VOLUME_SET;
import static net.kyma.EventType.PLAYER_PAUSE;
import static net.kyma.EventType.PLAYER_PLAY_FROM;
import static net.kyma.EventType.PLAYER_RESUME;
import static net.kyma.EventType.PLAYER_SET_SLIDER;
import static net.kyma.EventType.PLAYER_SET_VOLUME;
import static net.kyma.EventType.PLAYER_SOUND_LENGTH;
import static net.kyma.EventType.PLAYER_STOP;
import static net.kyma.EventType.PLAYLIST_NEXT;
import static net.kyma.EventType.PLAYLIST_PREVIOUS;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
public class PlayerPaneController {
    @Setter(AccessLevel.PACKAGE)
    private Slider volumeSlider;
    @Setter(AccessLevel.PACKAGE)
    private PlayButton playButton;
    @Setter(AccessLevel.PACKAGE)
    private Slider playbackProgress;
    @Setter(AccessLevel.PACKAGE)
    private Label timeLabel;
    private final Bus<EventType> bus;

    public void initialize() {
        playbackProgress.setMajorTickUnit(0.01D);
        bus.subscribingFor(GUI_VOLUME_SET).<Integer>accept(value -> volumeSlider.setValue(value)).subscribe();
        bus.message(GUI_VOLUME_GET).withResponse(GUI_VOLUME_SET).send();
        bus.message(PLAYER_SET_SLIDER).withContent(playbackProgress).send();
        bus.subscribingFor(PLAYER_SOUND_LENGTH).onFXThread().accept(this::updateTiming).subscribe();
        playButton.showPlay();
    }

    private void updateTiming(SoundFile soundFile) {
        long seconds = soundFile.getLength();
        timeLabel.setText(String.format("%s:%s", seconds / 60, seconds % 60));
    }

    void startOrPause() {
        if (playButton.isPaused()) {
            playButton.showPlay();
            bus.message(PLAYER_PAUSE).send();
        } else {
            playButton.showPause();
            bus.message(PLAYER_RESUME).send();
        }
    }

    void stop() {
        bus.message(PLAYER_STOP).send();
        playButton.showPlay();
    }

    public void playFrom(MouseEvent mouseEvent) {
        bus.message(PLAYER_PLAY_FROM)
              .withContent(round(playbackProgress.getMax() * (mouseEvent.getX() / playbackProgress.getWidth())))
              .send();
    }

    public void setVolume(MouseEvent mouseEvent)
    {
        bus.message(PLAYER_SET_VOLUME).withContent((int) volumeSlider.getValue()).send();
    }

    void playNext() {
        bus.message(PLAYLIST_NEXT).send();
    }

    public void playPrevious() {
        bus.message(PLAYLIST_PREVIOUS).send();
    }
}
