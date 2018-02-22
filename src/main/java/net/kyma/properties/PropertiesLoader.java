package net.kyma.properties;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyma.Loadable;

@Log4j2
public class PropertiesLoader implements Loadable {
    private Properties properties;
    @Getter
    private File file;

    @Override
    public void load() {
        properties = new Properties();
        try {
            file = new File("userProperties.properties");
            if (file.exists()) {
                properties.load(new BufferedInputStream(new FileInputStream(file)));
            } else {
                file.createNewFile();
            }
        } catch (IOException e) {
            log.error("Could not create file", e);
        }
    }

    public String getProperty(String key)
    {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        properties.put(key, value);
    }

    public void store(FileWriter writer) throws IOException
    {
        properties.store(writer, "kyma properties");
    }
}
