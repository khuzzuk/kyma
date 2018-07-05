package net.kyma.disk;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class FileAccessor extends Thread implements Loadable {
   private final Bus<EventType> bus;
   private Map<Path, FileOperation> fileOperations;
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
         log.info("New File Operation after closing: {}", operation.getPath());
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
      // if (fileOperations.containsKey(fileOperation.getPath())) {
      //    if (channel.isEmpty()) {
      //       wait(10);
      //    }
      //    channel.put(fileOperation);
      // }

      //fileOperations.put(fileOperation.getPath(), fileOperation);
      fileOperation.getOperation().run();
      //fileOperations.remove(fileOperation.getPath());
   }
}
