package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SlopedRoofPosition {

    HIGHEST_TWO_THIRDS("highest-two-thirds", "Upper half of the roof"),
    LOWEST_THIRD("lowest-third", "Lower half of the roof"),
    I_DO_NOT_KNOW("i-do-not-know", "I do not know / Somewhere else");

    private final String value;
    private final String displayName;

    SlopedRoofPosition(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SlopedRoofPosition fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Sloped roof position is null");
        }

        String normalized = input.trim().toLowerCase();

        for (SlopedRoofPosition position : values()) {
            if (position.value.equals(normalized)
                    || position.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return position;
            }
        }

        throw new IllegalArgumentException("Unsupported sloped roof position: " + input);
    }
}
