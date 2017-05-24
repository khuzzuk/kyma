package net.kyma.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.StringWriter;

@Log4j2
@Singleton
public class ColumnSerializer {

    @Inject
    private ObjectMapper objectMapper;

    public void setColumnsSettings(StringWriter stringWriter) throws IOException {
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.writeValue(stringWriter, Columns.class);
    }
}
