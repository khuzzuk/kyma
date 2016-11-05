package net.kyma.player;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.MultiContentPublisher;
import pl.khuzzuk.messaging.MultiContentSubscriber;

import java.io.IOException;
import java.util.Properties;

@RequiredArgsConstructor
public class PlayerManager {
    private final Bus bus;
    private final Properties messages;
    private MultiContentSubscriber subscriber;
    private MultiContentPublisher publisher;
    private Mp3Player currentPlayer;

    public void init() throws IOException {
        subscriber = bus.getMultiContentSubscriber();
        publisher = bus.getMultiContentPublisher();
        subscriber.subscribe(messages.getProperty("player.play.mp3"), this::playMp3);
        subscriber.subscribe(messages.getProperty("player.metadata.getLength"), () -> System.out.println(currentPlayer.getLength()));
        subscriber.subscribe(messages.getProperty("player.metadata.getCurrentTime"), () -> System.out.println(currentPlayer.playbackStatus()));
    }

    private void playMp3(String path) {
        if (currentPlayer != null) {
            currentPlayer.stop();
        }
        currentPlayer = new Mp3Player(path);
        currentPlayer.init();
        currentPlayer.start();
    }
}
