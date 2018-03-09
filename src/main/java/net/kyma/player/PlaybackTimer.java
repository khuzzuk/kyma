package net.kyma.player;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import pl.khuzzuk.messaging.Bus;

@SuppressWarnings("unused")
@Log4j2
@RequiredArgsConstructor
public class PlaybackTimer implements Loadable {
    @SuppressWarnings("FieldCanBeLocal")
    private static final int refreshLatency = 32;
    private final Bus<EventType> bus;
    private final PlayerManager playerManager;
    private BlockingQueue<TimerAction> channel;

    @Override
    public void load() {
        channel = new ArrayBlockingQueue<>(8);
        bus.setReaction(EventType.PLAYER_STOP_TIMER, () -> channel.offer(TimerAction.STOP));
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
                    channel.clear();
                    channel.add(TimerAction.CONTINUE);
                } else {
                    channel.add(TimerAction.CONTINUE);
                    playerManager.updateSlider();
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
