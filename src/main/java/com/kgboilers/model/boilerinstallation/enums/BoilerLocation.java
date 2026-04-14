package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerLocation {

    BASEMENT("basement"),
    GROUND_FLOOR("ground-floor"),
    FIRST_FLOOR("first-floor"),
    LOFT("loft"),
    GARAGE_OR_OUTSIDE("garage-outside"),
    SECOND_OR_HIGHER("second-plus");

    private final String value;

    BoilerLocation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BoilerLocation fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Location is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerLocation location : values()) {
            if (location.value.equals(normalized) || location.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return location;
            }
        }

        throw new IllegalArgumentException("Unsupported location: " + input);
    }
}