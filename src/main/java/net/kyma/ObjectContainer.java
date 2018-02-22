package net.kyma;

import lombok.AllArgsConstructor;
import pl.khuzzuk.messaging.Bus;

@AllArgsConstructor
public class ObjectContainer
{
   private Bus<EventType> bus;

   void putToContainer(EventType retrieveEvent, Object object)
   {

      bus.setResponse(retrieveEvent, () -> object);
   }

   void putLoadableToContainer(EventType retrieveEvent, Loadable loadable)
   {
      loadable.load();
      putToContainer(retrieveEvent, loadable);
   }
}
