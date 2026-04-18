package com.kgboilers.model.centralheatingquote.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InstallationPositionType {

    SAME_POSITION("same-position", "Same position"),
    DIFFERENT_POSITION("different-position", "Different position"),
    NO_EXISTING_ITEM("no-existing-item", "No existing item there");

    private final String value;
    private final String label;

    InstallationPositionType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static InstallationPositionType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Installation position is null");
        }

        String normalized = input.trim().toLowerCase();

        for (InstallationPositionType positionType : values()) {
            if (positionType.value.equals(normalized) || positionType.name().equalsIgnoreCase(normalized)) {
                return positionType;
            }
        }

        throw new IllegalArgumentException("Unsupported installation position: " + input);
    }
}
