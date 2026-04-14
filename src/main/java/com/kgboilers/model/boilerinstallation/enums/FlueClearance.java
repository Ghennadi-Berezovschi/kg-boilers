package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FlueClearance {

    THIRTY_CM_OR_MORE("yes"),
    LESS_THAN_THIRTY_CM("no"),
    UNSURE("unsure");

    private final String value;

    FlueClearance(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FlueClearance fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Flue clearance is null");
        }

        String normalized = input.trim().toLowerCase();

        for (FlueClearance clearance : values()) {
            if (clearance.value.equals(normalized)
                    || clearance.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return clearance;
            }
        }

        throw new IllegalArgumentException("Unsupported flue clearance: " + input);
    }
}
