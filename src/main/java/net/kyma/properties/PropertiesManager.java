package net.kyma.properties;

import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_GET;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_STORE;
import static net.kyma.EventType.GUI_VOLUME_GET;
import static net.kyma.EventType.GUI_WINDOW_SETTINGS;
import static net.kyma.EventType.GUI_WINDOW_SET_FRAME;
import static net.kyma.EventType.GUI_WINDOW_SET_FULLSCREEN;
import static net.kyma.EventType.GUI_WINDOW_SET_MAXIMIZED;
import static net.kyma.EventType.PLAYER_SET_VOLUME;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FRAME;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FULLSCREEN;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_MAXIMIZED;
import static net.kyma.EventType.RET_OBJECT_MAPPER;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class PropertiesManager implements Loadable
{
   private static final Path settingsPath = Paths.get("settings.json");

   private final Bus<EventType> bus;
   private ObjectMapper objectMapper;
   private PropertiesData propertiesData;

   @Override
   public void load()
   {
      bus.subscribingFor(RET_OBJECT_MAPPER).accept(this::setObjectMapper).subscribe();

      propertiesData = PropertiesData.defaultProperties();

      bus.subscribingFor(GUI_WINDOW_SETTINGS).then(this::windowGetSettings).subscribe();
      bus.subscribingFor(PROPERTIES_STORE_WINDOW_FRAME).accept(this::windowStoreRectangle).subscribe();
      bus.subscribingFor(PROPERTIES_STORE_WINDOW_MAXIMIZED)
            .<Boolean>accept(b -> {
               propertiesData.getUiProperties().setMaximized(b);
               store();
            }).subscribe();
      bus.subscribingFor(PROPERTIES_STORE_WINDOW_FULLSCREEN)
            .<Boolean>accept(b -> {
               propertiesData.getUiProperties().setFullScreen(b);
               store();
            }).subscribe();
      bus.subscribingFor(PLAYER_SET_VOLUME)
            .<Integer>accept(value -> {
               propertiesData.getPlayerProperties().setVolume(value);
               store();
            }).subscribe();
      bus.subscribingFor(GUI_VOLUME_GET)
            .withResponse(() -> propertiesData.getPlayerProperties().getVolume())
            .subscribe();
      bus.subscribingFor(GUI_CONTENTVIEW_SETTINGS_STORE).<List<UIProperties.ColumnDefinition>>accept(definitions -> {
         propertiesData.getUiProperties().setColumnDefinitions(definitions);
         store();
      }).subscribe();
      bus.subscribingFor(GUI_CONTENTVIEW_SETTINGS_GET)
            .withResponse(() -> propertiesData.getUiProperties().getColumnDefinitions())
            .subscribe();
   }

   private void setObjectMapper(ObjectMapper objectMapper)
   {
      this.objectMapper = objectMapper;
      if (Files.exists(settingsPath))
      {
         loadPropertiesData();
      }
      else
      {
         startDefaultProperties();
      }
   }

   private void windowGetSettings()
   {
      UIProperties uiProperties = propertiesData.getUiProperties();
      if (uiProperties.isFullScreen())
      {
         bus.message(GUI_WINDOW_SET_FULLSCREEN).send();
      }
      else if (uiProperties.isMaximized())
      {
         bus.message(GUI_WINDOW_SET_MAXIMIZED).send();
      }
      else
      {
         bus.message(GUI_WINDOW_SET_FRAME)
               .withContent(new Rectangle(
                     uiProperties.getX(),
                     uiProperties.getY(),
                     uiProperties.getWidth(),
                     uiProperties.getHeight()))
               .send();
      }
   }

   private void windowStoreRectangle(Rectangle rectangle)
   {
      UIProperties uiProperties = propertiesData.getUiProperties();
      uiProperties.setX(rectangle.x);
      uiProperties.setY(rectangle.y);
      uiProperties.setWidth(rectangle.width);
      uiProperties.setHeight(rectangle.height);
      store();
   }

   private synchronized void store()
   {
      try (OutputStream out = Files.newOutputStream(settingsPath))
      {
         objectMapper.writeValue(out, propertiesData);
      }
      catch (IOException e)
      {
         log.error("Cannot store settings", e);
      }
   }

   private void loadPropertiesData()
   {
      try (BufferedReader reader = Files.newBufferedReader(settingsPath))
      {
         propertiesData = objectMapper.readValue(reader, PropertiesData.class);
      }
      catch (IOException e)
      {
         log.error("Cannot read settings file", e);
      }
   }

   private void startDefaultProperties()
   {
      try
      {
         Files.createFile(settingsPath);
         store();
      }
      catch (IOException e)
      {
         log.error("Cannot create settings file", e);
      }
   }
}
