package net.kyma;

import java.util.concurrent.atomic.AtomicBoolean;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class IndexingFinishReporter
{
   private static AtomicBoolean indexingFinished = new AtomicBoolean();

   @After("execution(private void net.kyma.data.DataIndexer.indexSingleEntity(..))")
   public void catchIndexingFinished() {
      System.out.println("Indexing finished");
      indexingFinished.set(true);
   }

   public static void reset() {
      System.out.println("Indexing Reporter reset");
      indexingFinished.set(false);
   }

   public static boolean isIndexingFinished()
   {
      return indexingFinished.get();
   }
}
