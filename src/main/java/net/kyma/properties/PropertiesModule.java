package net.kyma.properties;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Log4j2
public class PropertiesModule extends AbstractModule {
    @Override
    protected void configure() {
        Properties properties = new Properties();
        try {
            File file = new File("/userProperties.properties");
            if (file.exists()) {
                properties.load(PropertiesModule.class.getResourceAsStream("/userProperties.properties"));
            } else {
                file.createNewFile();
            }
            bind(File.class).annotatedWith(Names.named("propertiesFile")).toInstance(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bind(Properties.class).annotatedWith(Names.named("userProperties")).toInstance(properties);
    }
}
