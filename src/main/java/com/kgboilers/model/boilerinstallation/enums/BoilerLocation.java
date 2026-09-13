package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerLocation {

    UTILITY_ROOM("utility-room", "Utility room"),
    KITCHEN("kitchen", "Kitchen"),
    GARAGE("garage", "Garage"),
    AIRING_CUPBOARD("airing-cupboard", "Airing cupboard"),
    BEDROOM("bedroom", "Bedroom"),
    LOFT_OR_ATTIC("loft-attic", "Loft or attic"),
    SOMEWHERE_ELSE("somewhere-else", "Other / Not sure");

    private final String value;
    private final String label;

    BoilerLocation(String value, String label) {
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

    @Override
    public String toString() {
        return label;
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
