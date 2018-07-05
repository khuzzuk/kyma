package net.kyma.player;

import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
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
public class FLACPlayer extends SPIPlayer {
   @Getter
   private long length;

   public FLACPlayer(SoundFile soundFile, Bus<EventType> bus) {
      super(soundFile, bus);
   }

   @Override
   Decoder getDecoder() throws SPIException {
      try {
         FLACInternalDecoder decoder = new FLACInternalDecoder();
         decoder.refresh();
         return decoder;
      } catch (IOException e) {
         throw new SPIException(e);
      }
   }

   private class FLACInternalDecoder implements Decoder
   {
      private FLACDecoder externalDecoder;
      private ByteData byteData;
      private StreamInfo info;
      private long current;

      private void refresh() throws IOException
      {
         externalDecoder = new FLACDecoder(new FileInputStream(soundFile.getPath()));
         info = externalDecoder.readStreamInfo();
         length = info.getTotalSamples();
      }

      @Override
      public boolean writeInto(SourceDataLine line) throws IOException
      {
         Frame frame = externalDecoder.readNextFrame();
         if (frame == null) return false;

         current = frame.header.sampleNumber;
         byteData = externalDecoder.decodeFrame(frame, byteData);
         line.write(byteData.getData(),0, byteData.getLen());
         return true;
      }

      @Override
      public AudioFormat getFormat()
      {
         return info.getAudioFormat();
      }

      @Override
      public long getCurrentPlaybackStatus()
      {
         return current;
      }

      @Override
      public void skipTo(long sampleNumber) throws IOException
      {
         if (current > sampleNumber)
         {
            refresh();
         }

         Frame frame = externalDecoder.readNextFrame();
         while (frame.header.sampleNumber < sampleNumber)
         {
            externalDecoder.readNextFrame();
         }
      }
   }
}
