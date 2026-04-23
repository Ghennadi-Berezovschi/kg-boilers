package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HorizontalFlueShape {

    SQUARE("square", "Square"),
    ROUND("round", "Round"),
    I_DO_NOT_KNOW("i-do-not-know", "I don't know");

    private final String value;
    private final String displayName;

    HorizontalFlueShape(String value, String displayName) {
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
    public static HorizontalFlueShape fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Horizontal flue shape is null");
        }

        String normalized = input.trim().toLowerCase();

        for (HorizontalFlueShape shape : values()) {
            if (shape.value.equals(normalized) || shape.name().toLowerCase().equals(normalized)) {
                return shape;
            }
        }

        throw new IllegalArgumentException("Unsupported horizontal flue shape: " + input);
    }
}
