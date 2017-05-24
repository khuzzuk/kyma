package net.kyma.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Log4j2
@Singleton
public class ColumnDeserializer {

    @Inject
    private ObjectMapper objectMapper;

    public Columns getColumnsSettings() throws IOException {
        return objectMapper.readValue(Files.readAllBytes(Paths.get("columns.json")), Columns.class);
    }
}
