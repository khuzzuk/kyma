package net.kyma.properties;

import static net.kyma.EventType.GUI_WINDOW_SETTINGS;
import static net.kyma.EventType.GUI_WINDOW_SET_FRAME;
import static net.kyma.EventType.GUI_WINDOW_SET_FULLSCREEN;
import static net.kyma.EventType.GUI_WINDOW_SET_MAXIMIZED;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FRAME;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_FULLSCREEN;
import static net.kyma.EventType.PROPERTIES_STORE_WINDOW_MAXIMIZED;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.kyma.EventType;
import net.kyma.Loadable;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import pl.khuzzuk.messaging.Bus;

@Log4j2
@AllArgsConstructor
public class PropertiesManager implements Loadable
{
    private Bus<EventType> bus;
    private PropertiesLoader propertiesLoader;

    @Override
    public void load() {
        bus.setReaction(GUI_WINDOW_SETTINGS, this::windowGetSettings);
        bus.setReaction(PROPERTIES_STORE_WINDOW_FRAME, this::windowStoreRectangle);
        bus.<Boolean>setReaction(PROPERTIES_STORE_WINDOW_MAXIMIZED, b -> set("player.window.maximized", b));
        bus.<Boolean>setReaction(PROPERTIES_STORE_WINDOW_FULLSCREEN, b -> set("player.window.fullScreen", b));
    }

    private void windowGetSettings() {

        if (BooleanUtils.toBoolean(propertiesLoader.getProperty("player.window.fullScreen"))) {
            bus.send(GUI_WINDOW_SET_FULLSCREEN);
            return;
        }

        String maximizedFromProperties = propertiesLoader.getProperty("player.window.maximized");
        if (BooleanUtils.toBoolean(maximizedFromProperties)) {
            bus.send(GUI_WINDOW_SET_MAXIMIZED);
            return;
        }

        String x = propertiesLoader.getProperty("player.window.x");
        String y = propertiesLoader.getProperty("player.window.y");
        String width = propertiesLoader.getProperty("player.window.width");
        String height = propertiesLoader.getProperty("player.window.height");

        if (NumberUtils.isParsable(x) && NumberUtils.isParsable(y)
                && NumberUtils.isParsable(width) && NumberUtils.isParsable(height)) {

            Rectangle rectangle = new Rectangle();
            rectangle.setRect(NumberUtils.toDouble(x), NumberUtils.toDouble(y),
                    NumberUtils.toDouble(width), NumberUtils.toDouble(height));

            bus.send(GUI_WINDOW_SET_FRAME, rectangle);
        }
    }

    private void windowStoreRectangle(Rectangle rectangle) {
        propertiesLoader.setProperty("player.window.height", String.valueOf(rectangle.getHeight()));
        propertiesLoader.setProperty("player.window.width", String.valueOf(rectangle.getWidth()));
        propertiesLoader.setProperty("player.window.x", String.valueOf(rectangle.getX()));
        propertiesLoader.setProperty("player.window.y", String.valueOf(rectangle.getY()));
        store();
    }

    private synchronized void store() {
        try (FileWriter writer = new FileWriter(propertiesLoader.getFile())) {
            propertiesLoader.store(writer);
        } catch (IOException e) {
            log.error("cannot write properties to file");
            log.error(e);
        }
    }

    private void set(String key, Boolean value) {
        propertiesLoader.setProperty(key, String.valueOf(Boolean.TRUE.equals(value)));
        store();
    }
}
