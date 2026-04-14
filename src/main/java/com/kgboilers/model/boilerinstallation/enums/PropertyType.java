package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyType {
    HOUSE("house"),
    FLAT("flat"),
    BUNGALOW("bungalow"),
    OTHER("other");

    private final String value;

    PropertyType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PropertyType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Property type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (PropertyType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported property type: " + input);
    }
}