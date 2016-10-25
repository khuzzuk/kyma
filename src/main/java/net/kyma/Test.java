package net.kyma;

import net.kyma.player.PlayerManager;
import pl.khuzzuk.messaging.BagPublisher;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.Message;
import pl.khuzzuk.messaging.Publisher;

import java.io.IOException;
import java.util.Properties;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        Bus bus = Bus.initializeBus();
        BagPublisher<String> publisher = bus.getBagPublisher();
        Properties messages = new Properties();
        messages.load(Test.class.getResourceAsStream("/messages.properties"));
        PlayerManager manager = new PlayerManager(bus, messages);
        manager.init();
        publisher.publish("Preludium.mp3", messages.getProperty("player.play.mp3"));
        Thread.sleep(1000);
        publisher.publish(bus.getBagMessage(messages.getProperty("player.metadata.getLength"), ""));
        while (true) {
            publisher.publish(bus.getBagMessage(messages.getProperty("player.metadata.getCurrentTime"), ""));
            Thread.sleep(500);
        }
    }
}
