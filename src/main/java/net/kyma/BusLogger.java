package net.kyma;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BusLogger extends PrintStream
{
   public BusLogger() throws FileNotFoundException
   {
      super("bus.log");
   }

   @Override
   public void println(String x)
   {
      log.debug(x);
   }

   @Override
   public void println(Object x)
   {
      log.debug(x);
   }
}
