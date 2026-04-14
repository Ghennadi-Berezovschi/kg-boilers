package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OwnershipType {
    HOMEOWNER("homeowner"),
    LANDLORD("landlord");

    private final String value;

    OwnershipType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OwnershipType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Ownership is null");
        }

        String normalized = input.trim().toLowerCase();

        for (OwnershipType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported ownership: " + input);
    }
}
