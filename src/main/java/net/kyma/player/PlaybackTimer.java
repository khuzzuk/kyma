package net.kyma.player;

import static net.kyma.EventType.METADATA_TIME_CURRENT;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import pl.khuzzuk.messaging.Bus;

@SuppressWarnings("unused")
@Log4j2
public class PlaybackTimer implements Loadable {
    @SuppressWarnings("FieldCanBeLocal")
    private final int refreshLatency = 32;
    private Bus<EventType> bus;
    private BlockingQueue<TimerAction> channel;

    public PlaybackTimer(Bus<EventType> bus)
    {
        this.bus = bus;
    }

    @Override
    public void load() {
        channel = new ArrayBlockingQueue<>(8);
        Thread t = new Thread(this::run);
        t.setDaemon(true);
        t.start();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void run() {
        try {
            while (true) {
                TimerAction action = channel.take();
                if (action == TimerAction.STOP) {
                    channel.clear();
                } else if (action == TimerAction.START) {
                    bus.sendMessage(EventType.METADATA_GET_LENGTH, EventType.METADATA_LENGTH);
                    channel.clear();
                    channel.add(TimerAction.CONTINUE);
                } else {
                    channel.add(TimerAction.CONTINUE);
                    bus.sendMessage(EventType.METADATA_GET_TIME_CURRENT, METADATA_TIME_CURRENT);
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
