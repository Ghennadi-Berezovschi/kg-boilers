package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FluePropertyDistance {

    LESS_THAN_ONE_METRE("less-than-1m"),
    MORE_THAN_ONE_METRE("more-than-1m");

    private final String value;

    FluePropertyDistance(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FluePropertyDistance fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Flue property distance is null");
        }

        String normalized = input.trim().toLowerCase();

        for (FluePropertyDistance distance : values()) {
            if (distance.value.equals(normalized)
                    || distance.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return distance;
            }
        }

        throw new IllegalArgumentException("Unsupported flue property distance: " + input);
    }
}
