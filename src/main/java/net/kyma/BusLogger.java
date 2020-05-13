package net.kyma;

import static net.kyma.EventType.SHOW_ALERT;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import pl.khuzzuk.messaging.Bus;

@Log4j2(topic = "bus")
public class BusLogger extends PrintStream {
   @Setter
   private Bus<EventType> bus;
   BusLogger() throws FileNotFoundException {
      super("bus.log");
   }

   @Override
   public void println(String x) {
      log.info(x);
   }

   @Override
   public void println(Object x){
      log.error("error found", (Throwable) x);
      bus.message(SHOW_ALERT).withContent(x.toString()).send();
   }
}
