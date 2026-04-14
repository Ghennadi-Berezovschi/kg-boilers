package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FlueLength {

    ZERO_TO_ONE("0-1"),
    TWO_TO_THREE("2-3"),
    FOUR_TO_FIVE("4-5"),
    SIX_TO_SEVEN("6-7"),
    SEVEN_PLUS("7+");

    private final String value;

    FlueLength(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FlueLength fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Flue length is null");
        }

        String normalized = input.trim().toLowerCase();

        for (FlueLength type : values()) {
            if (type.value.equals(normalized) ||
                    type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported flue length: " + input);
    }
}