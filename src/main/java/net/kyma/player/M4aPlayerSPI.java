package net.kyma.player;

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

import java.io.File;
import java.io.IOException;

@Log4j2
public class M4aPlayerSPI extends Mp3PlayerJLayer
{
   M4aPlayerSPI(SoundFile file, Bus<EventType> bus) {
      super(file, bus);
   }

   @Override
   Decoder getDecoder() throws SPIException {
      try {
         M4aDecoder decoder = new M4aDecoder();
         decoder.refresh(0);
         return decoder;
      } catch (Exception e) {
         throw new SPIException(e);
      }
   }

   private class M4aDecoder extends SPIPlayer.SPIDecoder {
      @Override
      void calculateLengths() throws IOException {
         try {
            AudioFile audioFile = AudioFileIO.read(new File(soundFile.getPath()));
            AudioHeader audioHeader = audioFile.getAudioHeader();
            length = audioHeader.getTrackLength() * 1000L;
            bytesTotal = audioHeader.getAudioDataLength().intValue();
         } catch (TagException | InvalidAudioFrameException e) {
            log.error("M4a file calculation error", e);
         } catch (CannotReadException | ReadOnlyFileException e) {
            throw new IOException(e);
         }
      }
   }
}
