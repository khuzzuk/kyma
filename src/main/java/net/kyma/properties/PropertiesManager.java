package net.kyma.properties;

import static net.kyma.EventType.FILES_EXECUTE;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_GET;
import static net.kyma.EventType.GUI_CONTENTVIEW_SETTINGS_STORE;
import static net.kyma.EventType.GUI_VOLUME_GET;
import static net.kyma.EventType.GUI_WINDOW_GET_FRAME;
import static net.kyma.EventType.GUI_WINDOW_IS_FULLSCREEN;
import static net.kyma.EventType.GUI_WINDOW_IS_MAXIMIZED;
import static net.kyma.EventType.GUI_WINDOW_SET_FRAME;
import static net.kyma.EventType.GUI_WINDOW_SET_FULLSCREEN;
import static net.kyma.EventType.GUI_WINDOW_SET_MAXIMIZED;
import static net.kyma.EventType.PLAYER_SET_VOLUME;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FRAME;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FULLSCREEN;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_MAXIMIZED;
import static net.kyma.EventType.RET_OBJECT_MAPPER;
import static net.kyma.EventType.SHOW_ALERT;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.disk.FileOperation;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@RequiredArgsConstructor
public class PropertiesManager implements Loadable
{
   private static final Path settingsPath = Paths.get("settings.json");

   private final Bus<EventType> bus;
   private ObjectMapper objectMapper;
   private PropertiesData propertiesData;

   private Runnable storeOperation = () -> {
      try (OutputStream out = Files.newOutputStream(settingsPath)) {
         objectMapper.writeValue(out, propertiesData);
      } catch (IOException e) {
         log.error("Cannot store settings", e);
      }
   };

   @Override
   public void load()
   {
      bus.subscribingFor(RET_OBJECT_MAPPER).accept(this::setObjectMapper).subscribe();

      propertiesData = PropertiesData.defaultProperties();

      subscribeForUi(PROPERTIES_STORE_WINDOW_MAXIMIZED, UIProperties::setMaximized);
      subscribeForUi(PROPERTIES_STORE_WINDOW_FULLSCREEN, UIProperties::setFullScreen);
      subscribeForUi(PROPERTIES_STORE_WINDOW_FRAME, this::setWindowRectangle);
      subscribeForUi(GUI_CONTENTVIEW_SETTINGS_STORE, UIProperties::setColumnDefinitions);
      reactForUi(GUI_WINDOW_GET_FRAME, uiProp -> new Rectangle(uiProp.getX(), uiProp.getY(), uiProp.getWidth(), uiProp.getHeight()));
      reactForUi(GUI_CONTENTVIEW_SETTINGS_GET, UIProperties::getColumnDefinitions);
      bus.subscribingFor(GUI_WINDOW_GET_FRAME)
            .then(() -> bus.message(GUI_WINDOW_SET_FRAME).withContent(getWindowRectangle()).send())
            .subscribe();
      bus.subscribingFor(GUI_WINDOW_IS_MAXIMIZED).then(() -> {
         if (propertiesData.getUiProperties().isMaximized()) bus.message(GUI_WINDOW_SET_MAXIMIZED).send();
      }).subscribe();
      bus.subscribingFor(GUI_WINDOW_IS_FULLSCREEN).then(() -> {
         if (propertiesData.getUiProperties().isFullScreen()) bus.message(GUI_WINDOW_SET_FULLSCREEN).send();
      }).subscribe();

      subscribeForPlayer(PLAYER_SET_VOLUME, PlayerProperties::setVolume);
      reactForPlayer(GUI_VOLUME_GET, PlayerProperties::getVolume);
   }

   private <T> void reactForUi(EventType event, Function<UIProperties, T> contentSupplier) {
      reactForProperty(event, () -> propertiesData.getUiProperties(), contentSupplier);
   }

   private <T> void reactForPlayer(EventType event, Function<PlayerProperties, T> contentSupplier) {
      reactForProperty(event, () -> propertiesData.getPlayerProperties(), contentSupplier);
   }

   private <T, U> void reactForProperty(EventType event, Supplier<U> propertySupplier, Function<U, T> retrieveFunction) {
      bus.subscribingFor(event).withResponse(() -> retrieveFunction.apply(propertySupplier.get())).subscribe();
   }

   private <T> void subscribeForUi(EventType event, BiConsumer<UIProperties, T> updateFunction) {
      subscribeForChange(event, () -> propertiesData.getUiProperties(), updateFunction);
   }

   private <T> void subscribeForPlayer(EventType event, BiConsumer<PlayerProperties, T> updateFunction) {
      subscribeForChange(event, () -> propertiesData.getPlayerProperties(), updateFunction);
   }

   private <T, U> void subscribeForChange(EventType event, Supplier<U> propertySupplier, BiConsumer<U, T> updateFunction) {
      bus.subscribingFor(event).<T>accept(val -> {
         updateFunction.accept(propertySupplier.get(), val);
         store();
      }).subscribe();
   }

   private void setObjectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      if (Files.exists(settingsPath)) {
         bus.message(FILES_EXECUTE).withContent(new FileOperation(settingsPath, this::loadPropertiesData)).send();
      } else {
         bus.message(FILES_EXECUTE).withContent(new FileOperation(settingsPath, this::startDefaultProperties)).send();
      }
   }

   private Rectangle getWindowRectangle() {
      UIProperties uiProperties = propertiesData.getUiProperties();
      return new Rectangle(uiProperties.getX(), uiProperties.getY(), uiProperties.getWidth(), uiProperties.getHeight());
   }

   private void setWindowRectangle(UIProperties uiProperties, Rectangle rectangle) {
      uiProperties.setX(rectangle.x);
      uiProperties.setY(rectangle.y);
      uiProperties.setWidth(rectangle.width);
      uiProperties.setHeight(rectangle.height);
   }

   private synchronized void store() {
      bus.message(FILES_EXECUTE).withContent(new FileOperation(settingsPath, storeOperation)).send();
   }

   private void loadPropertiesData() {
      try (BufferedReader reader = Files.newBufferedReader(settingsPath)) {
         propertiesData = objectMapper.readValue(reader, PropertiesData.class);
      } catch (IOException e) {
         bus.message(SHOW_ALERT).withContent(String.format("Cannot read settings file: %s", e.getMessage())).send();
      }
   }

   private void startDefaultProperties() {
      try {
         Files.createFile(settingsPath);
         store();
      } catch (IOException e) {
         bus.message(SHOW_ALERT).withContent(String.format("Cannot create settings file: %s", e.getMessage())).send();
      }
   }
}
