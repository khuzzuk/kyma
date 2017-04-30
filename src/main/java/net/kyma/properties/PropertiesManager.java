package net.kyma.properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer;
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

    @Inject
    @Named("messages")
    private Properties messages;

    private String lastAlbum;
    private String volume;
    private String musicPosition;

    private OutputStream output;

    PropertiesManager() {
    }

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

    // TODO: Dimension?
    public void initializationDimension() {
        //bus.setResponse("", );
    }

    public void windowDimension() {
        String x = messages.getProperty("player.window.dimension.set.x");
        String y = messages.getProperty("player.window.dimension.set.y");
        if(NumberUtils.isDigits(x) && NumberUtils.isDigits(y))
            if (NumberUtils.isParsable(x) && NumberUtils.isParsable(y)){
                int x1 = NumberUtils.toInt(x);
                int y1 = NumberUtils.toInt(y);
            }
    }
}
