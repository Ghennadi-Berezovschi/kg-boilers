package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Bedrooms {

    ONE("one"),
    TWO("two"),
    THREE("three"),
    FOUR("four"),
    FIVE("five"),
    SIX_PLUS("six-plus");

    private final String value;

    Bedrooms(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Bedrooms fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Bedrooms is null");
        }

        String normalized = input.trim().toLowerCase();

        for (Bedrooms b : values()) {
            if (b.value.equals(normalized) ||
                    b.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return b;
            }
        }

        throw new IllegalArgumentException("Unsupported bedrooms: " + input);
    }
}