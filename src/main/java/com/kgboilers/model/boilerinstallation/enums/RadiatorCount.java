package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RadiatorCount {

    ZERO_TO_FIVE("0-5"),
    SIX_TO_NINE("6-9"),
    TEN_TO_THIRTEEN("10-13"),
    FOURTEEN_TO_SIXTEEN("14-16"),
    SEVENTEEN_PLUS("17+");

    private final String value;

    RadiatorCount(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RadiatorCount fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Radiator count is null");
        }

        String normalized = input.trim().toLowerCase();

        for (RadiatorCount option : values()) {
            if (option.value.equals(normalized)
                    || option.name().toLowerCase().equals(normalized.replace("+", "_plus").replace("-", "_"))) {
                return option;
            }
        }

        throw new IllegalArgumentException("Unsupported radiator count: " + input);
    }
}
