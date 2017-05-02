package net.kyma.properties;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.math.NumberUtils;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;

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

        try (OutputStream output = new FileOutputStream("userProperties.properties")){
            properties.store(output, "User properties");
        } catch (IOException io) {
            log.error(io);
        }

        bus.setReaction(messages.getProperty("gui.window.settings"), this::windowSettings);
    }

    public void windowSettings() {
        String xFromProperties = messages.getProperty("player.window.x");
        String yFromProperties = messages.getProperty("player.window.y");

        String widthFromProperties = messages.getProperty("player.window.width");
        String heightFromProperties = messages.getProperty("player.window.height");

        String maximizedFromProperties = messages.getProperty("player.window.maximized");

        if(NumberUtils.isDigits(xFromProperties) && NumberUtils.isDigits(yFromProperties)
                && NumberUtils.isDigits(widthFromProperties) && NumberUtils.isDigits(heightFromProperties))
            if (NumberUtils.isParsable(xFromProperties) && NumberUtils.isParsable(yFromProperties)
                    && NumberUtils.isParsable(widthFromProperties) && NumberUtils.isParsable(heightFromProperties)){

                int x = NumberUtils.toInt(xFromProperties);
                int y = NumberUtils.toInt(yFromProperties);

                bus.send(properties.getProperty("player.window.x"), x);
                bus.send(properties.getProperty("player.window.y"), y);

                int width = NumberUtils.toInt(widthFromProperties);
                int height = NumberUtils.toInt(heightFromProperties);

                bus.send(properties.getProperty("player.window.width"), width);
                bus.send(properties.getProperty("player.window.height"), height);

                bus.send(properties.getProperty("player.window.maximized"), maximizedFromProperties);
            }
    }
}
