package net.kyma.properties;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Properties;

@Log4j2
public class PropertiesManager {
    @Inject
    private Bus bus;

    @Inject
    @Named("userProperties")
    private Properties properties;

    @Inject
    @Named("messages")
    private Properties messages;

    @Inject
    @Named("propertiesFile")
    private File propertiesFile;

    private OutputStream output;

    public void initializationProperties() {
        properties = new Properties();

        try (OutputStream output = new FileOutputStream("userProperties.properties")) {
            properties.store(output, "User properties");
        } catch (IOException io) {
            log.error(io);
        }

        bus.setReaction(messages.getProperty("gui.window.settings"), this::windowGetSettings);
        bus.setReaction(messages.getProperty("properties.window.store.frame"), this::windowStoreRectangle);
        bus.setReaction(messages.getProperty("properties.window.store.maximized"), this::windowMaximized);
    }

    private void windowGetSettings() {
        String maximizedFromProperties = messages.getProperty("player.window.maximized");

        if (BooleanUtils.toBoolean(maximizedFromProperties)) {
            bus.send(messages.getProperty("gui.window.set.maximized"));
            return;
        }

        String xFromProperties = messages.getProperty("player.window.x");
        String yFromProperties = messages.getProperty("player.window.y");
        String widthFromProperties = messages.getProperty("player.window.width");
        String heightFromProperties = messages.getProperty("player.window.height");

        if (NumberUtils.isDigits(xFromProperties) && NumberUtils.isDigits(yFromProperties)
                && NumberUtils.isDigits(widthFromProperties) && NumberUtils.isDigits(heightFromProperties)) {

            Rectangle rectangle = new Rectangle(NumberUtils.toInt(xFromProperties), NumberUtils.toInt(yFromProperties),
                    NumberUtils.toInt(widthFromProperties), NumberUtils.toInt(heightFromProperties));

            bus.send(messages.getProperty("gui.window.set.frame"), rectangle);
        }
    }

    private void windowStoreRectangle(Rectangle rectangle) {
        properties.setProperty("player.window.height", String.valueOf(rectangle.getHeight()));
        properties.setProperty("player.window.width", String.valueOf(rectangle.getWidth()));
        properties.setProperty("player.window.x", String.valueOf(rectangle.getX()));
        properties.setProperty("player.window.y", String.valueOf(rectangle.getY()));
    }

    private void windowMaximized(Boolean maximized) {
        properties.setProperty("player.window.maximized", String.valueOf(maximized.equals(Boolean.TRUE)));
    }
}
