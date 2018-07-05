package net.kyma.player;

import static net.kyma.EventType.FILES_EXECUTE;
import static net.kyma.EventType.SHOW_ALERT;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.disk.FileOperation;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public abstract class SPIPlayer implements Player {
    private static SPIPlayer currentPlayer;
    private static AtomicBoolean paused = new AtomicBoolean(false);
    private static AtomicLong skipTo = new AtomicLong(0);
    final SoundFile soundFile;
    final Bus<EventType> bus;
    private boolean closed;
    private SourceDataLine line;
    private float volume = 100;
    private Emitter emitter;
    private long currentPlaybackStatus;

    private static void globalPause() {
        paused.set(true);
    }

    private static void globalUnPause()
    {
        paused.set(false);
    }

    private static void setGlobalPlayer(SPIPlayer newPlayer)
    {
        currentPlayer = newPlayer;
    }

    public void start() {
        if (paused.get() && line != null) {
            globalUnPause();
            return;
        } else if (currentPlayer != null) {
            currentPlayer.closed = true;
        }
        emitter = new Emitter();
        bus.message(FILES_EXECUTE).withContent(new FileOperation(Paths.get(soundFile.getPath()), emitter)).send();
        setGlobalPlayer(this);
    }

    @Override
    public void stop() {
        closed = true;
    }

    @Override
    public void pause() {
        globalPause();
    }

    @Override
    public boolean isPaused() {
        return paused.get();
    }

    @Override
    public long playbackStatus() {
        return currentPlaybackStatus;
    }

    @Override
    public void startFrom(long frame) {
        skipTo.set(frame);
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

        @Override
        public void run() {
            try {
                initDecoding();
                while (decoder.writeInto(line)) {
                    if (closed) break;

                    if ((paused.get())) {
                        line.stop();
                        hold();
                        line.start();
                    }

                    long framesToSkip = skipTo.getAndSet(0);
                    if (framesToSkip > 0) {
                        line.stop();
                        emitter.decoder.skipTo(framesToSkip);
                        line.start();
                    }

                    currentPlaybackStatus = decoder.getCurrentPlaybackStatus();
                }
                line.drain();
                line.stop();
                line.close();
                if (!(closed)) {
                    bus.message(EventType.PLAYLIST_NEXT).send();
                }
            } catch (IOException e) {
                log.error("Accessing file error during playback", e);
                bus.message(SHOW_ALERT).withContent("No acces to file").send();
            } catch (Exception e) {
                log.error("Error during processing a file", e);
                bus.message(SHOW_ALERT).withContent("Cannot process sound").send();
            }
        }

        private void initDecoding() throws SPIException
        {
            try {
                decoder = getDecoder();
                AudioFormat format = decoder.getFormat();
                line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(volume);
            }
            catch (LineUnavailableException e)
            {
                log.error("problem with opening file", e);
                throw new SPIException(e);
            }
        }

        private void hold() {
            try {
                while (paused.get()) {
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                log.error("interrupted during pause removing", e);
                Thread.currentThread().interrupt();
            }
        }

    }

    abstract Decoder getDecoder() throws SPIException;

    interface Decoder
    {
        boolean writeInto(SourceDataLine line) throws IOException;
        AudioFormat getFormat();
        long getCurrentPlaybackStatus();
        void skipTo(long frame) throws IOException;
    }

    static void closePlayers() {
        if (currentPlayer != null) currentPlayer.closed = true;
    }
}
