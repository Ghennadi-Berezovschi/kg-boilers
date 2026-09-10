package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HeatOnlyConversion {
    YES("yes"),
    NO("no");

    private final String value;

    HeatOnlyConversion(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static HeatOnlyConversion fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Heat only conversion is null");
        }

        String normalized = input.trim().toLowerCase();

        for (HeatOnlyConversion conversion : values()) {
            if (conversion.value.equals(normalized) || conversion.name().toLowerCase().equals(normalized)) {
                return conversion;
            }
        }

        throw new IllegalArgumentException("Unsupported heat only conversion: " + input);
    }
}
