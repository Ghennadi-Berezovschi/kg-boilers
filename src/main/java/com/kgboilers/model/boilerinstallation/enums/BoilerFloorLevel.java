package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerFloorLevel {

    BASEMENT("basement"),
    GROUND_FLOOR("ground-floor"),
    FIRST_FLOOR("first-floor"),
    SECOND_OR_HIGHER("second-plus");

    private final String value;

    BoilerFloorLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BoilerFloorLevel fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Boiler floor level is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerFloorLevel floorLevel : values()) {
            if (floorLevel.value.equals(normalized)
                    || floorLevel.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return floorLevel;
            }
        }

        throw new IllegalArgumentException("Unsupported boiler floor level: " + input);
    }
}
