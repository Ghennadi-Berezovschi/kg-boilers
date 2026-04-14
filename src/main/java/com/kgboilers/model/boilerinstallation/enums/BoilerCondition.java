package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerCondition {

    NOT_WORKING("not-working"),
    OLD_INEFFICIENT("old-inefficient"),
    DOESNT_MEET_NEEDS("doesnt-meet-needs"),
    OTHER("other");

    private final String value;

    BoilerCondition(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BoilerCondition fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Boiler condition is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerCondition condition : values()) {
            if (condition.value.equals(normalized)
                    || condition.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return condition;
            }
        }

        throw new IllegalArgumentException("Unsupported boiler condition: " + input);
    }
}