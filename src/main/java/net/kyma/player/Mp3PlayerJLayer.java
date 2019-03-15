package net.kyma.player;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import org.tritonus.share.sampled.file.TAudioFileFormat;
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
public class Mp3PlayerJLayer extends SPIPlayer {
   @Getter
   long length;

   public Mp3PlayerJLayer(SoundFile soundFile, Bus<EventType> bus) {
      super(soundFile, bus);
   }

   @Override
   Decoder getDecoder() throws SPIException {
      try {
         Mp3Decoder decoder = new Mp3Decoder();
         decoder.refresh(0);
         return decoder;
      } catch (Exception e) {
         throw new SPIException(e);
      }
   }

   class Mp3Decoder implements Decoder {
      private AudioInputStream player;
      private final byte[] data = new byte[128];
      @Getter
      AudioFormat format;
      private long skipped;
      private long linePosition;
      int bytesTotal;
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

      void calculateLengths() throws IOException, UnsupportedAudioFileException {
         TAudioFileFormat rawFormat = (TAudioFileFormat) AudioSystem.getAudioFileFormat(new File(soundFile.getPath()));
         length = (long) rawFormat.properties().get("duration") / 1000; //microseconds to milliseconds
         bytesTotal = (int) rawFormat.properties().get("mp3.length.bytes");
      }

      @Override
      public synchronized boolean writeInto(SourceDataLine line) throws IOException
      {
         int bytesRead = player.read(data, 0, data.length);
         if (bytesRead == -1) return false;
         line.write(data, 0, data.length);
         linePosition = line.getMicrosecondPosition() / 1000;
         currentPlaybackStatus = linePosition + skipped;
         return true;
      }

      @Override
      public void skipTo(long toSkip) throws IOException
      {
         skipped = toSkip - linePosition;
         try {
            refresh(toSkip);
         }
         catch (UnsupportedAudioFileException e) {
            log.error("Cannot read file", e);
            bus.message(SHOW_ALERT).withContent("Cannot decode mp3 file").send();
         }
      }
   }
}
