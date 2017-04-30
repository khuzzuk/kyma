package net.kyma.properties;

import lombok.Getter;
import lombok.Setter;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesManager {
    @Inject
    private Bus bus;

    @Inject
    @Named("userProperties")
    private Properties properties;

    @Setter
    @Getter
    private String lastAlbum;

    @Setter
    @Getter
    private String volume;

    @Setter
    @Getter
    private String musicPosition;

    private OutputStream output;

    public void initializationProperties() {
        properties = new Properties();

        try {
            output = new FileOutputStream("userProperties.properties");

            properties.setProperty("user.music.lastAlbum",      lastAlbum);
            properties.setProperty("user.music.volume",         volume);
            properties.setProperty("user.music.musicPosition",  musicPosition);

            properties.store(output, "User properties");

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // TODO: Add this event on the bus programming pattern
    public void initializationConnect() {
        bus.send(properties.getProperty("user.music.lastAlbum"));
    }
}
