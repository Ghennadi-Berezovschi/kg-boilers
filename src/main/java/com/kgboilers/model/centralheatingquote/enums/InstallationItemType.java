package com.kgboilers.model.centralheatingquote.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InstallationItemType {

    RADIATOR("radiator", "Radiator"),
    TOWEL_RAIL("towel-rail", "Towel rail");

    private final String value;
    private final String label;

    InstallationItemType(String value, String label) {
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
    public static InstallationItemType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Installation item type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (InstallationItemType type : values()) {
            if (type.value.equals(normalized) || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported installation item type: " + input);
    }
}
