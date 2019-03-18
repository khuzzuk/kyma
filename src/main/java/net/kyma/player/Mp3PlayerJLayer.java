package net.kyma.player;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import pl.khuzzuk.messaging.Bus;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;

@Log4j2
public class Mp3PlayerJLayer extends SPIPlayer {
   private static final AudioFileReader MP3_READER = new MpegAudioFileReader();

   public Mp3PlayerJLayer(SoundFile soundFile, Bus<EventType> bus) {
      super(soundFile, bus);
   }

   @Override
   Decoder getDecoder() throws SPIException {
      try {
         Mp3Decoder decoder = new Mp3Decoder();
         decoder.refresh(0);
         return decoder;
      } catch (IOException | UnsupportedAudioFileException e) {
         throw new SPIException(e);
      }
   }

   class Mp3Decoder extends SPIPlayer.SPIDecoder {
      @Override
      AudioInputStream retrieveAudioInputStream(File file) throws IOException, UnsupportedAudioFileException {
         return MP3_READER.getAudioInputStream(file);
      }

      @Override
      void calculateLengths() throws IOException, UnsupportedAudioFileException {
         TAudioFileFormat rawFormat = (TAudioFileFormat) AudioSystem.getAudioFileFormat(new File(soundFile.getPath()));
         length = (long) rawFormat.properties().get("duration") / 1000; //microseconds to milliseconds
         bytesTotal = (int) rawFormat.properties().get("mp3.length.bytes");
      }
   }
}
