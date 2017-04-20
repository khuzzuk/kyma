package net.kyma.player;

import lombok.extern.log4j.Log4j2;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Log4j2
@Singleton
public class PlaybackTimer {
    private int refreshLatency = 32;
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    private BlockingQueue<TimerAction> channel;

    void init() {
        channel = new ArrayBlockingQueue<>(8);
        Thread t = new Thread(this::run);
        t.setDaemon(true);
        t.start();
    }

    void run() {
        try {
            while (true) {
                TimerAction action = channel.take();
                if (action == TimerAction.STOP) {
                    channel.clear();
                } else if (action == TimerAction.START) {
                    bus.send(messages.getProperty("player.metadata.getLength"));
                    channel.clear();
                    channel.add(TimerAction.CONTINUE);
                } else {
                    channel.add(TimerAction.CONTINUE);
                    bus.send(messages.getProperty("player.metadata.getCurrentTime"));
                    Thread.sleep(refreshLatency);
                }
            }
        } catch (InterruptedException e) {
            log.error("refreshing playback interrupted");
            log.error(e);
        }
    }

    void start() {
        channel.add(TimerAction.START);
    }

    public void stop() {
        channel.add(TimerAction.STOP);
    }

    private enum TimerAction {
        START, CONTINUE, STOP
    }
}
