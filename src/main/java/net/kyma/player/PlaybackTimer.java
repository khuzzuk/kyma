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
        channel = new ArrayBlockingQueue<>(64);
        bus.subscribingFor(EventType.PLAYER_STOP_TIMER).then(() -> channel.offer(TimerAction.STOP)).subscribe();
        Thread t = new Thread(this::run);
        t.setDaemon(true);
        t.start();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void run() {
        while (true) {
            try {
                TimerAction action = channel.take();
                switch (action)
                {
                    case STOP:
                        channel.clear();
                        break;
                    case START:
                        channel.clear();
                        channel.add(TimerAction.CONTINUE);
                        break;
                    default:
                        channel.add(TimerAction.CONTINUE);
                        playerManager.updateSlider();
                        Thread.sleep(refreshLatency);
                        break;
                }
            } catch (InterruptedException e) {
                log.error("refreshing playback interrupted", e);
            }
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
