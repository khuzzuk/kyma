package net.kyma.disk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
public class FileAccessor extends Thread implements Loadable {
   private final Bus<EventType> bus;
   private Map<SoundFile, FileOperation> fileOperations;
   private BlockingQueue<FileOperation> channel;
   private AtomicBoolean closed;

   @Override
   public void load() {
      fileOperations = new HashMap<>();
      channel = new LinkedBlockingQueue<>();
      closed = new AtomicBoolean(false);
      bus.subscribingFor(EventType.FILES_EXECUTE).accept(this::addOperation).subscribe();
      bus.subscribingFor(EventType.CLOSE).then(() -> closed.set(true)).subscribe();
      start();
   }

   private void addOperation(FileOperation operation) {
      if (closed.get()) {
         throw new IllegalStateException();
      }

      channel.offer(operation);
   }

   @Override
   @SneakyThrows(InterruptedException.class)
   public void run() {
      while (true) {
         if (closed.get() && channel.isEmpty()) {
            break;
         }

         FileOperation fileOperation = channel.take();
         executeFileOperation(fileOperation);
      }
   }

   private void executeFileOperation(FileOperation fileOperation) throws InterruptedException {
      if (fileOperations.containsKey(fileOperation.getSoundFile())) {
         if (channel.isEmpty()) {
            wait(10);
         }
         channel.put(fileOperation);
      }

      fileOperations.put(fileOperation.getSoundFile(), fileOperation);
      fileOperation.getOperation().run();
      fileOperations.remove(fileOperation.getSoundFile());
   }
}
