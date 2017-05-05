package net.kyma.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PropertiesField {
    MUSICPOSITION("musicPosition");

    private final String name;
}
