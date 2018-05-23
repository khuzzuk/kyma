package net.kyma;

import java.util.concurrent.atomic.AtomicBoolean;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class IndexingFinishReporter
{
   private static AtomicBoolean indexingFinished = new AtomicBoolean();

   @Before("execution(public void net.kyma.data.DataIndexer.load(..))")
   public void catchIndexingFinished()
   {
      System.out.println("\n\n\t\tWoven\n\n");
      indexingFinished.set(true);
   }

   public static void reset()
   {
      indexingFinished.set(false);
   }

   public static boolean isIndexingFinished()
   {
      return indexingFinished.get();
   }
}
