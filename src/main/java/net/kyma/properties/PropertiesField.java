package net.kyma.properties;

import lombok.Getter;

@Getter
public enum PropertiesField {
    MUSICPOSITION("musicPosition");

    private final String name;

    PropertiesField(String name) {
        this.name = name;
    }
}
