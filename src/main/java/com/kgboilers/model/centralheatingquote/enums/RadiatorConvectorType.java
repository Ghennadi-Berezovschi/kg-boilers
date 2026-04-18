package com.kgboilers.model.centralheatingquote.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RadiatorConvectorType {

    SINGLE_CONVECTOR("single-convector", "Single convector"),
    DOUBLE_CONVECTOR("double-convector", "Double convector");

    private final String value;
    private final String label;

    RadiatorConvectorType(String value, String label) {
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
    public static RadiatorConvectorType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Radiator convector type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (RadiatorConvectorType type : values()) {
            if (type.value.equals(normalized) || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported radiator convector type: " + input);
    }
}
