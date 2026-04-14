package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Relocation {

    YES("yes"),
    NO("no");

    private final String value;

    Relocation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Relocation fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Relocation is null");
        }

        String normalized = input.trim().toLowerCase();

        for (Relocation r : values()) {
            if (r.value.equals(normalized) ||
                    r.name().toLowerCase().equals(normalized)) {
                return r;
            }
        }

        throw new IllegalArgumentException("Unsupported relocation: " + input);
    }
}
