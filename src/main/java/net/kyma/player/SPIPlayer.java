package net.kyma.player;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public abstract class SPIPlayer implements Player {
    private static ExecutorService thread = Executors.newFixedThreadPool(1);
    private static BlockingQueue<Object> meetingPoint = new SynchronousQueue<>();
    private static SPIPlayer currentPlayer;
    private Bus<EventType> bus;
    private boolean closed;
    private static boolean paused;
    private SourceDataLine line;
    private float volume = 100;
    private Emitter emitter;
    private long currentPlaybackStatus;

    SPIPlayer(Bus<EventType> bus) {
        this.bus = bus;
    }

    public void start() {
        try {
            if (paused && line != null) {
                meetingPoint.take();
                paused = false;
                return;
            } else if (currentPlayer != null) {
                currentPlayer.closed = true;
            }
            emitter = new Emitter();
            thread.submit(emitter);
            currentPlayer = this;
        }
        catch (InterruptedException e)
        {
            log.error("Error during starting FLAC Player", e);
        }
    }

    @Override
    public void stop() {
        if (emitter != null)
        {
            closed = true;
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public long playbackStatus() {
        return currentPlaybackStatus;
    }

    @Override
    public void startFrom(long frame) {
        paused = true;
        try {
            meetingPoint.take();
            emitter.decoder.skipTo(frame);
            paused = false;
            line.start();
        } catch (InterruptedException e) {
            log.error("interrupted pausing operation");
        }
        catch (IOException e)
        {
            log.error("Error during reading a file", e);
        }
    }

    @Override
    public void setVolume(int percent) {
        volume = (float) (Math.log10((float) percent / 100f) * 50);
        Optional.ofNullable(emitter)
              .map(Emitter::getControl)
              .ifPresent(control -> control.setValue(volume));
    }

    private class Emitter implements Runnable {

        @Getter
        private FloatControl control;
        private Decoder decoder;
        private AudioFormat format;

        @Override
        public void run() {
            try {
                initDecoding();
                while (decoder.writeInto(line)) {
                    if (closed) {
                        break;
                    }
                    if ((paused)) {
                        line.stop();
                        meetingPoint.offer(this);
                        hold();
                        line.start();
                    }
                    currentPlaybackStatus = decoder.getCurrentPlaybackStatus();
                }
                line.drain();
                line.stop();
                line.close();
                if (!(closed)) {
                    bus.send(EventType.PLAYLIST_NEXT);
                }
            } catch (LineUnavailableException e) {
                log.error("problem with opening file", e);
            }
            catch (IOException e) {
                log.error("Accessing file error during playback", e);
            }
            catch (Exception e)
            {
                log.error("Error during processing a file", e);
            }
        }

        private void initDecoding() throws Exception
        {
            decoder = getDecoder();
            format = decoder.getFormat();
            line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            control.setValue(volume);
        }

        private void hold() {
            try {
                while (paused) {
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    abstract Decoder getDecoder() throws Exception;

    interface Decoder
    {
        boolean writeInto(SourceDataLine line) throws IOException;
        AudioFormat getFormat();
        long getCurrentPlaybackStatus();
        void skipTo(long frame) throws IOException;
    }

    static void closePlayers() {
        thread.shutdownNow();
    }
}
