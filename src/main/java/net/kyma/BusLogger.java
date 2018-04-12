package net.kyma;

import static net.kyma.EventType.SHOW_ALERT;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import pl.khuzzuk.messaging.Bus;

@Log4j2
public class BusLogger extends PrintStream {
   @Setter
   private Bus<EventType> bus;
   BusLogger() throws FileNotFoundException {
      super("bus.log");
      this.bus = bus;
   }

   @Override
   public void println(String x) {
      log.debug(x);
   }

   @Override
   public void println(Object x){
      log.debug(x);
      bus.message(SHOW_ALERT).withContent("Internal error").send();
   }
}
