package net.kyma.player;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PlayingScheduler
{
   private static ExecutorService thread = Executors.newFixedThreadPool(1);
   private static PlayAction current;

   static synchronized void play(PlayAction playAction)
   {
      stopCurrent();
      current = playAction;
      thread.submit(playAction::play);
   }

   static synchronized void stopCurrent()
   {
      if (current != null) current.stop();
   }

   @RequiredArgsConstructor
   static class PlayAction
   {
      @Getter
      private boolean paused;
      private boolean stopped;
      private static BlockingQueue<Object> meetingPoint = new SynchronousQueue<>();
      private final StreamSoundAction action;
      private final Runnable onFinish;

      void play()
      {
         while (true)
         {
            if (paused)
            {
               try
               {
                  meetingPoint.put(this);
               }
               catch (InterruptedException e)
               {
                  log.error("Playing interrupted", e);
               }
            }
            else if (stopped)
            {
               onFinish.run();
               break;
            }

            boolean isFinished = !action.stream();
            if (isFinished)
            {
               onFinish.run();
               break;
            }
         }
      }

      void stop()
      {
         stopped = true;
      }

      void pause()
      {
         paused = true;
      }

      void resume()
      {
         paused = false;
         try
         {
            meetingPoint.take();
         }
         catch (InterruptedException e)
         {
            log.error("Playing interrupted", e);
         }
      }
   }

   interface StreamSoundAction
   {
      boolean stream();
   }
}
