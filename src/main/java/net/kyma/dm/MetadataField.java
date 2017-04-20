package net.kyma.dm;

import lombok.Getter;

@Getter
public enum MetadataField {
    TITLE("title"),
    PATH("path"),
    INDEXED_PATH("indexedPath"),
    FILE_NAME("fileName"),
    RATE("rate");

    private final String name;

    MetadataField(String name) {
        this.name = name;
    }
}
