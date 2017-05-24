package net.kyma.properties;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import net.kyma.dm.MetadataField;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
public class Columns {
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<MetadataField, Double> columnsSize;
}
