package net.kyma;

import java.util.concurrent.atomic.AtomicBoolean;

import net.kyma.dm.SoundFile;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class IndexingFinishReporter
{
   private static AtomicBoolean indexingFinished = new AtomicBoolean();
   private static PropertyContainer<SoundFile> indexedFile;

   @After("execution(private void net.kyma.data.DataIndexer.indexSingleEntity(..)) && args(soundFile)")
   public void catchIndexingFinished(SoundFile soundFile) {
      System.out.println("Indexing finished");
      if (indexedFile != null) indexedFile.setValue(soundFile);
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

   public static void setPropertyContainer(PropertyContainer<SoundFile> soundFilePropertyContainer) {
      indexedFile = soundFilePropertyContainer;
   }
}
