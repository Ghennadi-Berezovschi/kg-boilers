package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FluePosition {

    UNDER_STRUCTURE("under-structure"),
    OPEN_AREA("open-area");

    private final String value;

    FluePosition(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FluePosition fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Flue position is null");
        }

        String normalized = input.trim().toLowerCase();

        for (FluePosition type : values()) {
            if (type.value.equals(normalized) ||
                    type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported flue position: " + input);
    }
}