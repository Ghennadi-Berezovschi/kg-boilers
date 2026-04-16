package com.kgboilers.model.centralheatingquote.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MagneticFilterStatus {

    YES_HAS("yes-has", "Yes, it has one"),
    NO_DOES_NOT_HAVE("no-does-not-have", "No, it does not have one"),
    DO_NOT_KNOW("do-not-know", "I do not know what it is");

    private final String value;
    private final String label;

    MagneticFilterStatus(String value, String label) {
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
    public static MagneticFilterStatus fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Magnetic filter status is null");
        }

        String normalized = input.trim().toLowerCase();

        for (MagneticFilterStatus status : values()) {
            if (status.value.equals(normalized) || status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unsupported magnetic filter status: " + input);
    }
}
