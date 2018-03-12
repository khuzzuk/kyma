package net.kyma.player;

import java.io.FileInputStream;
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
import net.kyma.dm.SoundFile;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class FLACPlayer implements Player {
    private static ExecutorService thread = Executors.newFixedThreadPool(1);
    private static BlockingQueue<Object> meetingPoint = new SynchronousQueue<>();
    private static FLACPlayer currentPlayer;
    private Bus<EventType> bus;
    private SoundFile file;
    private FLACDecoder decoder;
    private AudioFormat format;
    private boolean closed;
    private static boolean paused;
    private SourceDataLine line;
    @Getter
    private long length;
    private float volume = 100;
    private volatile long current;
    private Emitter emitter;

    FLACPlayer(SoundFile file, Bus<EventType> bus) {
        this.file = file;
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
            refreshDecoder();
            line = AudioSystem.getSourceDataLine(format);
            emitter = new Emitter();
            thread.submit(emitter);
            currentPlayer = this;
        } catch (LineUnavailableException e) {
            log.error("problem with opening flac file");
            log.error(e);
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
        return current;
    }

    @Override
    public void startFrom(long frame) {
        paused = true;
        try {
            meetingPoint.take();
        } catch (InterruptedException e) {
            log.error("interrupted pausing operation");
        }
        emitter.skipTo(frame);
    }

    @Override
    public void setVolume(int percent)
    {
        volume = -30f * (1 - ((float) percent / 100f));
        Optional.ofNullable(emitter)
              .map(Emitter::getControl)
              .ifPresent(floatControl -> floatControl.setValue(volume));
    }

    private void refreshDecoder() {
        try {
            decoder = new FLACDecoder(new FileInputStream(file.getPath()));
            StreamInfo info = decoder.readStreamInfo();
            length = info.getTotalSamples();
            format = info.getAudioFormat();
        } catch (IOException e) {
            log.error("problem with opening flac file");
            log.error(e);
        }
    }
    private class Emitter implements Runnable {

        @Getter
        private FloatControl control;

        @Override
        public void run() {
            try {
                line.open(format);
                line.start();
                control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(volume);
                Frame frame;
                while ((frame = decoder.readNextFrame()) != null) {
                    if (closed) {
                        break;
                    }
                    if ((paused)) {
                        line.stop();
                        meetingPoint.offer(this);
                        hold();
                        line.start();
                    }
                    current = frame.header.sampleNumber;
                    ByteData byteData = decoder.decodeFrame(frame, null);
                    line.write(byteData.getData(), 0, byteData.getLen());
                }
                line.close();
                if (!(closed)) {
                    bus.send(EventType.PLAYLIST_NEXT);
                }
            } catch (LineUnavailableException | IOException e) {
                log.error("problem with opening flac file");
                log.error(e);
            }
        }

        private void skipTo(long sampleNumber) {
            if (current > sampleNumber) {
                refreshDecoder();
            }

            try {
                Frame frame = decoder.readNextFrame();
                while (frame.header.sampleNumber < sampleNumber) {
                    decoder.readNextFrame();
                }
                paused = false;
                line.start();
            } catch (IOException e) {
                log.error("problem with opening flac file");
                log.error(e);
            }
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

    static void closeFLACPlayers() {
        thread.shutdownNow();
    }
}
