package net.kyma.properties;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.extern.log4j.Log4j2;
import net.kyma.BusModule;
import pl.khuzzuk.messaging.Bus;

import java.io.IOException;
import java.util.Properties;

@Log4j2
public class PropertiesModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Bus.class).toInstance(Bus.initializeBus());
        Properties properties = new Properties();
        try {
            properties.load(BusModule.class.getResourceAsStream("/userProperties.properties"));
        } catch (IOException e) {
            log.fatal("bus setup fatal error, exit program");
            log.fatal(e);
            e.printStackTrace();
            System.exit(-1);
        }
        bind(Properties.class).annotatedWith(Names.named("userProperties")).toInstance(properties);
    }
}
