package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerLocation {

    UTILITY_ROOM("utility-room"),
    KITCHEN("kitchen"),
    GARAGE("garage"),
    AIRING_CUPBOARD("airing-cupboard"),
    BEDROOM("bedroom"),
    LOFT_OR_ATTIC("loft-attic"),
    SOMEWHERE_ELSE("somewhere-else");

    private final String value;

    BoilerLocation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BoilerLocation fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Boiler location is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerLocation location : values()) {
            if (location.value.equals(normalized)
                    || location.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return location;
            }
        }

        throw new IllegalArgumentException("Unsupported boiler location: " + input);
    }
}
