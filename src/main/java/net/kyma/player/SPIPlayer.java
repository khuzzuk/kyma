package net.kyma.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.disk.FileOperation;
import net.kyma.dm.SoundFile;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import pl.khuzzuk.messaging.Bus;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static net.kyma.EventType.FILES_EXECUTE;
import static net.kyma.EventType.SHOW_ALERT;

@Log4j2
@RequiredArgsConstructor
public class SPIPlayer implements Player {
    private static SPIPlayer currentPlayer;
    private static boolean paused = false;
    private static AtomicLong skipTo = new AtomicLong(0);
    final SoundFile soundFile;
    final Bus<EventType> bus;
    private boolean closed;
    private SourceDataLine line;
    private float volume = 100;
    private Emitter emitter;
    @Getter
    long length;

    private static void globalPause() {
        paused = true;
    }

    private static void globalUnPause() {
        paused = false;
    }

    private static void setGlobalPlayer(SPIPlayer newPlayer) {
        currentPlayer = newPlayer;
    }

    public void start() {
        if (paused && line != null) {
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
        return paused;
    }

    @Override
    public long playbackStatus() {
        return emitter.decoder != null ? emitter.decoder.getCurrentPlaybackStatus() : 0;
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

                    if (paused) {
                        line.stop();
                        hold();
                        line.start();
                    }

                    if (skipTo.get() > 0) {
                        line.stop();
                        emitter.decoder.skipTo(skipTo.getAndSet(0));
                        line.start();
                    }
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
            } catch (SPIException e) {
                log.error("Error during processing a file", e);
                bus.message(SHOW_ALERT).withContent("Cannot process sound").send();
            }
        }

        void initDecoding() throws SPIException {
            try {
                decoder = getDecoder();
                AudioFormat format = decoder.getFormat();
                line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(volume);
            } catch (LineUnavailableException e) {
                log.error("problem with opening file", e);
                throw new SPIException(e);
            }
        }

        private void hold() {
            try {
                while (paused) {
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                log.error("interrupted during pause removing", e);
                Thread.currentThread().interrupt();
            }
        }

    }

    Decoder getDecoder() throws SPIException {
        try {
            SPIDecoder decoder = new SPIDecoder();
            decoder.refresh(0);
            return decoder;
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new SPIException(e);
        }
    }

    interface Decoder {
        boolean writeInto(SourceDataLine line) throws IOException;
        AudioFormat getFormat();
        long getCurrentPlaybackStatus();
        void skipTo(long frame) throws IOException;
    }

    class SPIDecoder implements Decoder {
        AudioInputStream player;
        private byte[] data = new byte[8];
        @Getter
        AudioFormat format;
        private long skipped;
        private long linePosition;
        int bytesTotal;

        public long getCurrentPlaybackStatus() {
            return linePosition + skipped;
        }

        AudioInputStream retrieveAudioInputStream(File file) throws IOException, UnsupportedAudioFileException {
            return AudioSystem.getAudioInputStream(file);
        }

        void refresh(long toSkip) throws IOException, UnsupportedAudioFileException {
            AudioInputStream rawAudio = retrieveAudioInputStream(new File(soundFile.getPath()));
            AudioFormat audioFormat = rawAudio.getFormat();
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
                    audioFormat.getChannels(), audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false);
            calculateLengths();

            player = AudioSystem.getAudioInputStream(format, rawAudio);
            long bytesToSkip = (long) (bytesTotal * ((double) (toSkip) / length));
            player.skip(bytesToSkip);
        }

        void calculateLengths() throws IOException, UnsupportedAudioFileException {
            try {
                AudioFile audioFile = AudioFileIO.read(new File(soundFile.getPath()));
                AudioHeader audioHeader = audioFile.getAudioHeader();
                length = audioHeader.getTrackLength() * 1000L;
                int frameSize = getFormat().getFrameSize();
                bytesTotal = audioHeader.getTrackLength() * audioHeader.getSampleRateAsNumber() * frameSize;
                data = new byte[frameSize];
            } catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                throw new UnsupportedAudioFileException(e.getMessage());
            }
        }

        @Override
        public synchronized boolean writeInto(SourceDataLine line) throws IOException {
            int bytesRead = player.read(data, 0, data.length);
            if (bytesRead == -1) return false;
            line.write(data, 0, data.length);
            linePosition = line.getMicrosecondPosition() / 1000;
            return true;
        }

        @Override
        public void skipTo(long toSkip) throws IOException {
            skipped = toSkip - linePosition;
            try {
                refresh(toSkip);
            } catch (UnsupportedAudioFileException e) {
                log.error("Cannot read file", e);
                bus.message(SHOW_ALERT).withContent("Cannot decode mp3 file").send();
            }
        }
    }

    static void closePlayers() {
        if (currentPlayer != null) currentPlayer.closed = true;
    }
}
