package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BathShowerCount {

    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR_PLUS("4+");

    private final String value;

    BathShowerCount(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BathShowerCount fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Bath/shower count is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BathShowerCount option : values()) {
            if (option.value.equals(normalized)
                    || option.name().toLowerCase().equals(normalized.replace("+", "_plus"))) {
                return option;
            }
        }

        throw new IllegalArgumentException("Unsupported bath/shower count: " + input);
    }
}
