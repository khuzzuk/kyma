package net.kyma.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import net.kyma.dm.MetadataField;
import pl.khuzzuk.messaging.Bus;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
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
    private LinkedHashMap<MetadataField, Double> linkedHashMap;
    private static File file;

    public void init()
    {
        objectMapper = new ObjectMapper();
        file = new File("columnsProperties.json");
        if (!file.exists()) {
            log.fatal("File is not exists.");
            System.exit(-1);
        }

        bus.setReaction(messages.getProperty("properties.columns.settings.store"), this::serializeJson);
        bus.setReaction(messages.getProperty("properties.collumns.settings.get"), this::deserializeJson);
    }

    private synchronized void serializeJson()
    {
        TypeReference<LinkedHashMap<MetadataField, Double>> typeReference =
                new TypeReference<LinkedHashMap<MetadataField, Double>>() {};
        try {
            linkedHashMap = objectMapper.readValue(file, typeReference);
        } catch (IOException e) {
            log.fatal("Can not serialize user properties from file.");
            log.fatal(e);
            e.printStackTrace();
        }
    }

    private synchronized void deserializeJson()
    {
        try {
            objectMapper.writeValue(file, linkedHashMap);
        } catch (IOException e) {
            log.fatal("Can not deserialize user properties to file.");
            log.fatal(e);
            e.printStackTrace();
        }
    }
}
