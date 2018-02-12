package net.kyma.player;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.SoundFile;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import pl.khuzzuk.messaging.Bus;

@Log4j2
class Mp3PlayerFX implements Player
{
   private AudioInputStream player;
   private final Bus bus;
   private final Properties messages;
   @Getter
   private long length;
   private long skipped;
   @Getter
   private final String path;
   @Getter
   private boolean paused;
   private boolean stopped;
   private boolean internalPaused;
   private byte[] data;
   private int bytesTotal;
   private int bytesRead;
   private int bytesWritten;
   private SourceDataLine line;
   private static ExecutorService pool = Executors.newFixedThreadPool(1);

   Mp3PlayerFX(SoundFile file, Bus bus, Properties messages)
   {
      this.path = file.getPath();
      this.bus = bus;
      this.messages = messages;
      data = new byte[4096];
   }

   @Override
   public void start()
   {
      start(skipped);
   }

   private void start(long millis)
   {
      paused = false;
      stopped = false;
      skipped = millis;

      try
      {
         AudioInputStream rawAudio = AudioSystem.getAudioInputStream(new File(path));
         AudioFormat audioFormat = rawAudio.getFormat();
         AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
               audioFormat.getChannels(), audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false);
         player = AudioSystem.getAudioInputStream(format, rawAudio);
         calculateLength();

         line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, player.getFormat()));
         line.open(player.getFormat());
         line.start();
         bytesRead = (int) player.skip((long) (bytesTotal * ((double) (millis) / length)));

         pool.execute(this::stream);
      }
      catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
      {
         e.printStackTrace();
      }
   }

   private void stream()
   {
      internalPaused = false;
      while (bytesRead > -1 &&
            !paused &&
            !stopped)
      {
         try
         {
            bytesRead = player.read(data, 0, data.length);
            bytesWritten = bytesRead != -1
                  ? line.write(data, 0, bytesRead)
                  : -1;
         }
         catch (IOException e)
         {
            e.printStackTrace();
            bytesRead = -1;
         }
      }

      internalPaused = true;
      skipped = paused ? playbackStatus() : 0;
      close();

      if (bytesRead < 0)
      {
         bus.send(messages.getProperty("playlist.next"));
      }
   }

   private void close()
   {
      if (bytesRead < 0 || stopped)
      {
         try
         {
            line.drain();
            line.stop();
            line.close();
            player.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }

   @Override
   public void stop()
   {
      stopped = true;
   }

   @Override
   public void pause()
   {
      paused = true;
   }

   @Override
   public long playbackStatus()
   {
      return line != null
            ? line.getMicrosecondPosition() / 1000 + skipped
            : 0;
   }

   @Override
   public void startFrom(long millis)
   {
      if (paused)
      {
         close();
      }
      else
      {
         stop();
      }
      hold();
      start(millis);
   }

   private void calculateLength() throws IOException, UnsupportedAudioFileException
   {
      if (player != null)
      {
         TAudioFileFormat format = (TAudioFileFormat) AudioSystem.getAudioFileFormat(new File(path));
         length = (long) format.properties().get("duration") / 1000; //microseconds to milliseconds
         bytesTotal = (int) format.properties().get("mp3.length.bytes");
      }
   }

   private void hold()
   {
      while (internalPaused)
      {
         try
         {
            Thread.sleep(1);
         }
         catch (InterruptedException e)
         {
            log.error(e);
         }
      }
   }
}
