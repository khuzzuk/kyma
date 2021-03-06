package net.kyma.data;

import static net.kyma.EventType.DATA_STORE_ITEM;
import static net.kyma.EventType.PLAYER_PLAY;

import lombok.AllArgsConstructor;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@AllArgsConstructor
public class PlayCounter implements Loadable {
    private Bus<EventType> bus;

    @Override
    public void load()
    {
        bus.subscribingFor(PLAYER_PLAY).accept(this::addToSoundFile).subscribe();
    }

    private void addToSoundFile(SoundFile soundFile) {
        soundFile.setCounter(soundFile.getCounter() + 1);
        bus.message(DATA_STORE_ITEM).withContent(soundFile).send();
    }
}
