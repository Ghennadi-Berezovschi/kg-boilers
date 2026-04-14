package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerType {

    COMBI("combi"),
    SYSTEM("system"),
    HEAT_ONLY("heat-only"),
    OTHER("other");

    private final String value;

    BoilerType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BoilerType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Boiler type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported boiler type: " + input);
    }
}