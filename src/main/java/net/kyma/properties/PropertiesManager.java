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
        String x = messages.getProperty("player.window.dimension.x");
        String y = messages.getProperty("player.window.dimension.y");
        if(NumberUtils.isDigits(x) && NumberUtils.isDigits(y))
            if (NumberUtils.isParsable(x) && NumberUtils.isParsable(y)){
                int x1 = NumberUtils.toInt(x);
                int y1 = NumberUtils.toInt(y);
            }
    }
}
