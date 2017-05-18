package net.kyma.data;

import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Properties;

@Singleton
public class PlayCounter {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;

    public void init() {
        bus.setReaction(messages.getProperty("player.play.mp3"), this::addToSoundFile);
    }

    private void addToSoundFile(SoundFile soundFile) {
        soundFile.setCounter(soundFile.getCounter() + 1);
        bus.send(messages.getProperty("data.index.item"), soundFile);
    }
}
