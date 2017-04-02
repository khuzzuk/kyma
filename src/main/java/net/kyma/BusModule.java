package net.kyma;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.extern.log4j.Log4j2;
import pl.khuzzuk.messaging.Bus;

import java.io.IOException;
import java.util.Properties;

@Log4j2
public class BusModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Bus.class).toInstance(Bus.initializeBus());
        Properties messages = new Properties();
        try {
            messages.load(Test.class.getResourceAsStream("/messages.properties"));
        } catch (IOException e) {
            log.fatal("bus setup fatal error, exit program");
            log.fatal(e);
            e.printStackTrace();
            System.exit(-1);
        }
        bind(Properties.class).annotatedWith(Names.named("messages")).toInstance(messages);
    }
}
