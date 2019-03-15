package net.kyma.player;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
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
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static net.kyma.EventType.SHOW_ALERT;

@Log4j2
public class OggPlayerSPI extends SPIPlayer {
    private long length;
    private int bytesTotal;

    public OggPlayerSPI(SoundFile soundFile, Bus<EventType> bus) {
        super(soundFile, bus);
    }

    @Override
    Decoder getDecoder() throws SPIException {
        try {
            OggDecoder oggDecoder = new OggDecoder();
            oggDecoder.refresh(0);
            return oggDecoder;
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new SPIException(e);
        }
    }

    void calculateLengths() throws IOException, UnsupportedAudioFileException {
        try {
            AudioFile audioFile = AudioFileIO.read(new File(soundFile.getPath()));
            AudioHeader audioHeader = audioFile.getAudioHeader();
            length = audioHeader.getTrackLength() * 1000L;
            bytesTotal = audioHeader.getTrackLength() * audioHeader.getSampleRateAsNumber() * 4;
        } catch (TagException | InvalidAudioFrameException e) {
            log.error("M4a file calculation error", e);
        } catch (CannotReadException | ReadOnlyFileException e) {
            throw new IOException(e);
        }
    }

    @Override
    public long getLength() {
        return length;
    }

    class OggDecoder implements Decoder {
        @Getter
        AudioFormat format;
        private AudioInputStream player;
        private final byte[] data = new byte[128];
        private long skipped;
        private long linePosition;
        @Getter
        private long currentPlaybackStatus;

        void refresh(long toSkip) throws IOException, UnsupportedAudioFileException {
            calculateLengths();

            AudioInputStream rawAudio = AudioSystem.getAudioInputStream(new File(soundFile.getPath()));
            AudioFormat audioFormat = rawAudio.getFormat();
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
                    audioFormat.getChannels(), audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false);

            player = AudioSystem.getAudioInputStream(format, rawAudio);
            long bytesToSkip = (long) (bytesTotal * ((double) (toSkip) / length));
            if (player.skip(bytesToSkip) != bytesToSkip) {
                log.warn("Wrong number of bytes were skipped when determining new sound position");
            }
        }

        @Override
        public boolean writeInto(SourceDataLine line) throws IOException {
            int bytesRead = player.read(data, 0, data.length);
            if (bytesRead == -1) return false;
            line.write(data, 0, data.length);
            linePosition = line.getMicrosecondPosition() / 1000;
            currentPlaybackStatus = linePosition + skipped;
            return true;
        }

        @Override
        public void skipTo(long frame) throws IOException {
            skipped = frame - linePosition;
            try {
                refresh(frame);
            }
            catch (UnsupportedAudioFileException e) {
                log.error("Cannot read file", e);
                bus.message(SHOW_ALERT).withContent("Cannot decode mp3 file").send();
            }
        }
    }
}
