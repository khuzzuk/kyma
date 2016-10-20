package net.kyma.player;

import lombok.RequiredArgsConstructor;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.MultiContentPublisher;
import pl.khuzzuk.messaging.MultiContentSubscriber;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RequiredArgsConstructor
public class PlayerManager {
    private final Bus bus;
    private final Properties messages;
    private MultiContentSubscriber subscriber;
    private MultiContentPublisher publisher;
    private Map<String, Mp3Player> players;

    public void init() throws IOException {
        subscriber = bus.getMultiContentSubscriber();
        publisher = bus.getMultiContentPublisher();
        subscriber.subscribe(messages.getProperty("player.play.mp3"), this::playMp3);
        players = new HashMap<>();
    }

    private void playMp3(String path) {
        Mp3Player player = new Mp3Player(path);
        players.put(path, player);
        player.start();
    }
}
