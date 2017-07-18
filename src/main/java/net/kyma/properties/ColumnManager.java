package net.kyma.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.MetadataField;
import pl.khuzzuk.messaging.Bus;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.*;

@Log4j2
public class ColumnManager {
    @Inject
    private Bus bus;

    @Inject
    @Named("messages")
    private Properties messages;

    @Inject
    private ObjectMapper objectMapper;

    private static File file;

    public void init() {
        file = new File(getClass().getResource("/columnsProperties.json").getFile());

        bus.setReaction(messages.getProperty("properties.columns.settings.store"), this::serializeJson);
        bus.setReaction(messages.getProperty("properties.columns.settings.get"),
                () -> Optional.ofNullable(deserializeJson())
                        .ifPresent(columns -> bus.send(messages.getProperty("gui.columns.set"), columns)));
    }

    private synchronized void serializeJson(Map<MetadataField, Double> columns) {
        try {
            objectMapper.writeValue(file, columns);
        } catch (IOException e) {
            log.error("Can not deserialize user properties to file.");
            log.error(e);
            e.printStackTrace();
        }
    }

    private synchronized LinkedHashMap<MetadataField, Double> deserializeJson() {
        try {
            return objectMapper.readValue(file, new TypeReference<LinkedHashMap<MetadataField, Double>>() {});
        } catch (IOException e) {
            log.error("Can not serialize user properties from file.");
            log.error(e);
            e.printStackTrace();
        }
        return null;
    }
}
