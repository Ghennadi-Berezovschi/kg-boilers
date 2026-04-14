package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerPosition {

    FLOOR_STANDING("floor-standing"),
    WALL_MOUNTED("wall-mounted");

    private final String value;

    BoilerPosition(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BoilerPosition fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Boiler position is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerPosition position : values()) {
            if (position.value.equals(normalized) || position.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return position;
            }
        }

        throw new IllegalArgumentException("Unsupported boiler position: " + input);
    }
}
