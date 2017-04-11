package net.kyma;

import net.kyma.player.PlayerManager;
import pl.khuzzuk.messaging.Bus;

import java.io.IOException;
import java.util.Properties;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        Bus bus = Bus.initializeBus();
        Properties messages = new Properties();
        messages.load(Test.class.getResourceAsStream("/messages.properties"));
        PlayerManager manager = new PlayerManager();
        manager.init();
        bus.send(messages.getProperty("player.play.mp3"), "Preludium.mp3");
        Thread.sleep(1000);
        bus.send(messages.getProperty("player.metadata.getLength"));
        while (true) {
            bus.send(messages.getProperty("player.metadata.getCurrentTime"));
            Thread.sleep(500);
        }
    }
}
