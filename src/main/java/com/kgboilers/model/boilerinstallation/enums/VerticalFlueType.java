package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VerticalFlueType {

    SLOPED_ROOF("sloped-roof", "Sloped roof"),
    FLAT_ROOF("flat-roof", "Flat roof");

    private final String value;
    private final String displayName;

    VerticalFlueType(String value, String displayName) {
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
    public static VerticalFlueType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Vertical flue type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (VerticalFlueType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported vertical flue type: " + input);
    }
}
