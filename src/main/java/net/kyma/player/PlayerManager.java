package net.kyma.player;

import static net.kyma.EventType.CLOSE;
import static net.kyma.EventType.GUI_VOLUME_SET;
import static net.kyma.EventType.PLAYER_PAUSE;
import static net.kyma.EventType.PLAYER_PLAY;
import static net.kyma.EventType.PLAYER_PLAY_FROM;
import static net.kyma.EventType.PLAYER_RESUME;
import static net.kyma.EventType.PLAYER_SET_SLIDER;
import static net.kyma.EventType.PLAYER_SET_VOLUME;
import static net.kyma.EventType.PLAYER_STOP;

import javafx.scene.control.Slider;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class PlayerManager implements Loadable
{
   private final Bus<EventType> bus;
   private PlaybackTimer timer;
   private Player currentPlayer;
   @Setter
   private Slider slider;
   private int volume = 100;

   @Override
   public void load()
   {
      bus.subscribingFor(PLAYER_SET_SLIDER).accept(this::setSlider).subscribe();
      bus.subscribingFor(PLAYER_PLAY).accept(this::playMp3).subscribe();
      bus.subscribingFor(PLAYER_PAUSE).then(this::pauseMp3).subscribe();
      bus.subscribingFor(PLAYER_STOP).then(this::stopMp3).subscribe();
      bus.subscribingFor(PLAYER_RESUME).then(this::resume).subscribe();
      bus.subscribingFor(CLOSE).then(this::stopMp3).subscribe();
      bus.subscribingFor(CLOSE).then(SPIPlayer::closePlayers).subscribe();
      bus.subscribingFor(PLAYER_PLAY_FROM).accept(this::startFrom).subscribe();
      bus.subscribingFor(PLAYER_SET_VOLUME).accept(this::setVolume).subscribe();
      bus.subscribingFor(GUI_VOLUME_SET).accept(this::setVolume).subscribe();

      timer = new PlaybackTimer(bus, this);
      timer.load();
   }

   private synchronized void playMp3(SoundFile file)
   {
      if (currentPlayer != null)
      {
         if (currentPlayer.isPaused())
         {
            currentPlayer.start();
            timer.start();
            return;
         }
         else
         {
            currentPlayer.stop();
         }
      }
      currentPlayer = file.getFormat().getPlayer(file, bus);
      if (currentPlayer == null)
      {
         //TODO send communicate to the user
         return;
      }
      log.info("start play");
      currentPlayer.start();
      currentPlayer.setVolume(volume);
      timer.start();
   }

   private synchronized void resume()
   {
      if (currentPlayer != null && currentPlayer.isPaused())
      {
         currentPlayer.start();
         timer.start();
      }
      else
      {
         bus.message(EventType.PLAYLIST_NEXT).send();
      }
   }

   private synchronized void stopMp3()
   {
      timer.stop();
      if (currentPlayer != null)
      {
         currentPlayer.stop();
         currentPlayer = null;
      }
   }

   private synchronized void pauseMp3()
   {
      timer.stop();
      if (currentPlayer != null)
      {
         currentPlayer.pause();
      }
   }

   private synchronized void startFrom(Long millis)
   {
      if (currentPlayer != null)
      {
         currentPlayer.startFrom(millis);
         slider.setMax(currentPlayer.getLength());
         timer.start();
      }
   }

   private void setVolume(int percent)
   {
      volume = percent;
      if (currentPlayer != null)
      {
         currentPlayer.setVolume(percent);
      }
   }

   void updateSlider()
   {
      if (currentPlayer != null)
      {
         slider.setMax(currentPlayer.getLength());
         slider.setValue(currentPlayer.playbackStatus());
      }
   }
}
