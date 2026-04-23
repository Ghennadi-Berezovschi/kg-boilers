package com.kgboilers.model.boilerrepair.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PowerFlushStatus {

    YES_DONE("yes-done", "Yes, it was done"),
    NO_NOT_DONE("no-not-done", "No, it was not done"),
    DO_NOT_KNOW("do-not-know", "I do not know what it is");

    private final String value;
    private final String label;

    PowerFlushStatus(String value, String label) {
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
    public static PowerFlushStatus fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Power flush status is null");
        }

        String normalized = input.trim().toLowerCase();

        for (PowerFlushStatus status : values()) {
            if (status.value.equals(normalized) || status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unsupported power flush status: " + input);
    }
}
