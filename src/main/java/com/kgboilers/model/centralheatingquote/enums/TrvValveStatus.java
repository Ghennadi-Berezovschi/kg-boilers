package com.kgboilers.model.centralheatingquote.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TrvValveStatus {

    YES("yes", "Yes"),
    NO("no", "No"),
    NOT_ALL_OF_THEM("not-all-of-them", "Not all of them"),
    DO_NOT_KNOW("do-not-know", "I do not know");

    private final String value;
    private final String label;

    TrvValveStatus(String value, String label) {
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
    public static TrvValveStatus fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("TRV valve status is null");
        }

        String normalized = input.trim().toLowerCase();

        for (TrvValveStatus status : values()) {
            if (status.value.equals(normalized) || status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unsupported TRV valve status: " + input);
    }
}
