package net.kyma.dm;

import lombok.Getter;

import java.time.Year;

@Getter
public enum MetadataField {
    TITLE("title"),
    PATH("path"),
    INDEXED_PATH("indexedPath"),
    FILE_NAME("fileName"),
    RATE("rate"),
    YEAR("year");

    private final String name;

    MetadataField(String name) {
        this.name = name;
    }
}
