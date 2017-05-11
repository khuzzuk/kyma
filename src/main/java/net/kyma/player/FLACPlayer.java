package net.kyma.player;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import pl.khuzzuk.messaging.Bus;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.*;

@Log4j2
public class FLACPlayer implements Player {
    private static ExecutorService thread = Executors.newFixedThreadPool(1);
    private static BlockingQueue<Object> meetingPoint = new SynchronousQueue<>();
    private Bus bus;
    private Properties messages;
    private SoundFile file;
    private FLACDecoder decoder;
    private AudioFormat format;
    private static boolean paused, closed;
    private SourceDataLine line;
    @Getter
    private long length;
    private volatile long current;
    private Emitter emitter;

    FLACPlayer(SoundFile file, Bus bus, Properties messages) {
        this.file = file;
        this.bus = bus;
        this.messages = messages;
    }

    public void start() {
        try {
            if (paused && line != null) {
                paused = false;
                return;
            } else {
                closed = true;
            }
            refreshDecoder();
            line = AudioSystem.getSourceDataLine(format);
            emitter = new Emitter();
            closed = false;
            thread.submit(emitter);
        } catch (LineUnavailableException e) {
            log.error("problem with opening flac file");
            log.error(e);
        }
    }

    @Override
    public void stop() {
        closed = true;
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

        @Override
        public void run() {
            try {
                line.open(format);
                line.start();
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
                    bus.send(messages.getProperty("playlist.next"));
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