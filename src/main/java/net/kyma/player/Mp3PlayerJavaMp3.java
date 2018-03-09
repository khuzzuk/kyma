package net.kyma.player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import fr.delthas.javamp3.Sound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class Mp3PlayerJavaMp3 implements Player
{
   private final SoundFile soundFile;
   private final Bus<EventType> bus;
   private Sound sound;
   @Getter
   private long length;
   private byte[] buffer = new byte[1024];

   @Override
   public void start()
   {
      try
      {
         length = AudioFileIO.read(new File(soundFile.getPath())).getAudioHeader().getAudioDataLength();
         BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get(soundFile.getPath())));
         try
         {
            sound = new Sound(stream);
         }
         catch (Throwable throwable)
         {
            throwable.printStackTrace();
         }
         //AudioInputStream inputStream = new AudioInputStream(new Mp3InputStream(), sound.getAudioFormat(), getLength() / 2);
         SourceDataLine line = (SourceDataLine) AudioSystem
               .getLine(new DataLine.Info(SourceDataLine.class, sound.getAudioFormat()));
         line.open();
         line.start();

         PlayingScheduler.play(new PlayingScheduler.PlayAction(this::read, line::close));

      }
      catch (IOException e)
      {
         log.error("Cannot read mp3 file", e);
      }
      catch (LineUnavailableException e)
      {
         log.error("System cannot play sounds", e);
      }
      catch (CannotReadException | ReadOnlyFileException e)
      {
         log.error("Cannot read file", e);
      }
      catch (InvalidAudioFrameException | TagException e)
      {
         log.error("File format corrupted", e);
      }
   }

   @Override
   public void stop()
   {

   }

   @Override
   public void pause()
   {

   }

   @Override
   public boolean isPaused()
   {
      return false;
   }

   @Override
   public long playbackStatus()
   {
      return 0;
   }

   @Override
   public void startFrom(long millis)
   {

   }

   @Override
   public long getLength()
   {
      return 0;
   }

   private boolean read()
   {
      try
      {
         int read = sound.read(buffer);

         if (read < 0)
         {
            bus.send(EventType.PLAYLIST_NEXT);
         }

         return read > -1;
      }
      catch (IOException e)
      {
         log.error("Error during playback from file", e);
         return false;
      }
   }

   private class Mp3InputStream extends InputStream
   {
      private int pos = 1024;
      private int limit = 1024;
      private byte[] buffer = new byte[1024];
      @Override
      public int read() throws IOException
      {
         if (limit >= pos)
         {
            limit = sound.read(buffer);
            pos = 0;
         }
         if (limit == -1)
         {
            return -1;
         }
         return buffer[pos++];
      }
   }
}
