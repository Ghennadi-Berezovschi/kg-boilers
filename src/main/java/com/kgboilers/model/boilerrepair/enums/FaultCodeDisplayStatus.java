package com.kgboilers.model.boilerrepair.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.kgboilers.exception.boilerrepairquote.UnsupportedFaultCodeDisplayException;

public enum FaultCodeDisplayStatus {

    YES_SHOWING("yes-showing", "Yes, there is a fault code, message or signal"),
    NO_NOT_SHOWING("no-not-showing", "No, nothing is showing"),
    DO_NOT_KNOW("do-not-know", "I do not know");

    private final String value;
    private final String label;

    FaultCodeDisplayStatus(String value, String label) {
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
    public static FaultCodeDisplayStatus fromValue(String input) {
        if (input == null) {
            throw new UnsupportedFaultCodeDisplayException("Fault code answer is null");
        }

        String normalized = input.trim().toLowerCase();

        for (FaultCodeDisplayStatus status : values()) {
            if (status.value.equals(normalized)
                    || status.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return status;
            }
        }

        throw new UnsupportedFaultCodeDisplayException("Unsupported fault code answer: " + input);
    }
}
