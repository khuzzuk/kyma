package net.kyma.player;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class Mp3PlayerJLayer extends SPIPlayer
{
   private SoundFile soundFile;
   @Getter
   private long length;
   private long skipped;
   @Getter
   private boolean paused;
   private long currentPlaybackStatus;

   Mp3PlayerJLayer(SoundFile file, Bus<EventType> bus)
   {
      super(bus);
      this.soundFile = file;
   }

   @Override
   public long playbackStatus()
   {
      return currentPlaybackStatus;
   }

   @Override
   Decoder getDecoder() throws Exception
   {
      Mp3Decoder decoder = new Mp3Decoder();
      decoder.refresh(0);
      return decoder;
   }

   private class Mp3Decoder implements Decoder
   {
      private AudioInputStream player;
      private byte[] data = new byte[4096];
      @Getter
      AudioFormat format;
      private long skipped;
      private int bytesTotal;

      private void refresh(long toSkip) throws IOException, UnsupportedAudioFileException {
         skipped = toSkip;

         calculateLengths();

         AudioInputStream rawAudio = AudioSystem.getAudioInputStream(new File(soundFile.getPath()));
         AudioFormat audioFormat = rawAudio.getFormat();
         format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
               audioFormat.getChannels(), audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false);

         player = AudioSystem.getAudioInputStream(format, rawAudio);
         player.skip((long) (bytesTotal * ((double) (toSkip) / length)));
      }

      private void calculateLengths()
            throws IOException, UnsupportedAudioFileException
      {
         TAudioFileFormat format = (TAudioFileFormat) AudioSystem.getAudioFileFormat(new File(soundFile.getPath()));
         length = (long) format.properties().get("duration") / 1000; //microseconds to milliseconds
         bytesTotal = (int) format.properties().get("mp3.length.bytes");
      }

      @Override
      public boolean writeInto(SourceDataLine line) throws IOException
      {
         int bytesRead = player.read(data, 0, data.length);
         if (bytesRead == -1) return false;
         line.write(data, 0, data.length);
         currentPlaybackStatus = line.getMicrosecondPosition() / 1000 + skipped;
         return true;
      }

      @Override
      public long getCurrentPlaybackStatus()
      {
         return 0;
      }

      @Override
      public void skipTo(long toSkip) throws IOException
      {
         skipped = toSkip;
         try
         {
            refresh(toSkip);
         }
         catch (UnsupportedAudioFileException e)
         {
            log.error("Cannot read file", e);
         }
      }
   }
}
