package net.kyma.properties;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;

import java.awt.*;
import java.io.*;

import java.util.Properties;

@Log4j2
public class PropertiesManager {
    @Inject
    private Bus bus;

    @Inject
    @Named("messages")
    private Properties messages;

    @Inject
    @Named("userProperties")
    private Properties properties;

    @Inject
    @Named("propertiesFile")
    private File propertiesFile;

    public void initializeProperties() {
        bus.setReaction(messages.getProperty("gui.window.settings"), this::windowGetSettings);
        bus.setReaction(messages.getProperty("properties.window.store.frame"), this::windowStoreRectangle);
        bus.<Boolean>setReaction(messages.getProperty("properties.window.store.maximized"),
                b -> set("player.window.maximized", b));
        bus.<Boolean>setReaction(messages.getProperty("properties.window.store.fullScreen"),
                b -> set("player.window.fullScreen", b));
    }

    private void windowGetSettings() {

        if (BooleanUtils.toBoolean(properties.getProperty("player.window.fullScreen"))) {
            bus.send(messages.getProperty("gui.window.set.fullScreen"));
            return;
        }

        String maximizedFromProperties = properties.getProperty("player.window.maximized");
        if (BooleanUtils.toBoolean(maximizedFromProperties)) {
            bus.send(messages.getProperty("gui.window.set.maximized"));
            return;
        }

        String x = properties.getProperty("player.window.x");
        String y = properties.getProperty("player.window.y");
        String width = properties.getProperty("player.window.width");
        String height = properties.getProperty("player.window.height");

        if (NumberUtils.isParsable(x) && NumberUtils.isParsable(y)
                && NumberUtils.isParsable(width) && NumberUtils.isParsable(height)) {

            Rectangle rectangle = new Rectangle();
            rectangle.setRect(NumberUtils.toDouble(x), NumberUtils.toDouble(y),
                    NumberUtils.toDouble(width), NumberUtils.toDouble(height));

            bus.send(messages.getProperty("gui.window.set.frame"), rectangle);
        }
    }

    private void windowStoreRectangle(Rectangle rectangle) {
        properties.setProperty("player.window.height", String.valueOf(rectangle.getHeight()));
        properties.setProperty("player.window.width", String.valueOf(rectangle.getWidth()));
        properties.setProperty("player.window.x", String.valueOf(rectangle.getX()));
        properties.setProperty("player.window.y", String.valueOf(rectangle.getY()));
        store();
    }

    private synchronized void store() {
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            properties.store(writer, "kyma properties");
        } catch (IOException e) {
            log.error("cannot write properties to file");
            log.error(e);
        }
    }

    private void windowMaximized(Boolean maximized) {
        properties.setProperty("player.window.maximized", String.valueOf(maximized.equals(Boolean.TRUE)));
        store();
    }

    private void windowFullScreen(Boolean fullScreen) {
        properties.setProperty("player.window.maximized", String.valueOf(fullScreen.equals(Boolean.TRUE)));
        store();
    }

    private void set(String key, Boolean value) {
        properties.setProperty(key, String.valueOf(value.equals(Boolean.TRUE)));
        store();
    }
}
