package net.kyma;

import net.kyma.player.PlayerManager;
import pl.khuzzuk.messaging.BagPublisher;
import pl.khuzzuk.messaging.Bus;

import java.io.IOException;
import java.util.Properties;

public class Test {
    public static void main(String[] args) throws IOException {
        Bus bus = Bus.initializeBus();
        BagPublisher<String> publisher = bus.getBagPublisher();
        Properties messages = new Properties();
        messages.load(Test.class.getResourceAsStream("/messages.properties"));
        PlayerManager manager = new PlayerManager(bus, messages);
        manager.init();
        publisher.publish("Preludium.mp3", messages.getProperty("player.play.mp3"));
    }
}
