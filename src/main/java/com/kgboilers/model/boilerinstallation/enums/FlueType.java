package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FlueType {

    VERTICAL("vertical"),
    HORIZONTAL("horizontal");

    private final String value;

    FlueType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FlueType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Flue type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (FlueType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported flue type: " + input);
    }
}
