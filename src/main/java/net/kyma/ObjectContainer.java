package net.kyma;

import java.util.Collection;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.kyma.properties.PropertiesLoader;
import net.kyma.properties.PropertiesManager;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
public class ObjectContainer
{
   private final Bus<EventType> bus;
   private Collection<Loadable> loadables;

   void createContainer()
   {
      loadables = new LinkedList<>();

      putToContainer(EventType.RET_OBJECT_MAPPER, new ObjectMapper());

      //Properties
      putToContainer(EventType.RET_PROPERTIES_MANAGER, new PropertiesManager(bus));

      loadables.forEach(Loadable::load);
   }

   void putToContainer(EventType retrieveEvent, Object object)
   {
      bus.setResponse(retrieveEvent, () -> object);
   }

   private void putToContainer(EventType retrieveEvent, Loadable loadable)
   {
      bus.setResponse(retrieveEvent, () -> loadable);
      loadables.add(loadable);
   }
}
