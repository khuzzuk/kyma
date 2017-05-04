package net.kyma.player;

public interface Player {
    void start();

    void stop();

    void pause();

    boolean isPaused();

    long playbackStatus();

    void startFrom(long millis);

    long getLength();
}
