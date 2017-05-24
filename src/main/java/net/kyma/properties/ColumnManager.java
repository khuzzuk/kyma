package net.kyma.properties;

import lombok.extern.log4j.Log4j2;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Properties;

@Log4j2
public class ColumnManager {

    @Inject
    private Bus bus;

    @Inject
    @Named("messages")
    private Properties messages;

    @Inject
    private ColumnDeserializer deserializer;

    @Inject
    private ColumnSerializer serializer;

    public void init() {
        bus.setReaction(messages.getProperty("properties.collumns.settings.get"), () -> {
            try {
                bus.send(messages.getProperty("properties.collumns.settings.get"), deserializer.getColumnsSettings());
            } catch (IOException e) {
                log.error("cannot read properties from file");
                log.error(e);
            }
        });
    }
}
