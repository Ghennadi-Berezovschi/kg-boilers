package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RelocationDistance {

    ZERO_TO_ONE("0-1"),
    TWO_TO_THREE("2-3"),
    FOUR_TO_FIVE("4-5"),
    SIX_TO_SEVEN("6-7"),
    SEVEN_PLUS("7+");

    private final String value;

    RelocationDistance(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RelocationDistance fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Distance is null");
        }

        String normalized = input.trim().toLowerCase();

        for (RelocationDistance d : values()) {
            if (d.value.equals(normalized) || d.name().toLowerCase().equals(normalized)) {
                return d;
            }
        }

        throw new IllegalArgumentException("Unsupported distance: " + input);
    }
}
