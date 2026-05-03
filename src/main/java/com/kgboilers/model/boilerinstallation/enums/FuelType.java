package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedFuelException;

public enum FuelType {
    GAS("gas", "Gas"),
    LPG("lpg", "LPG"),
    OIL("oil", "Oil"),
    ELECTRIC("electric", "Electric"),
    UNKNOWN_OTHER("unknown-other", "I don't know / Other");

    private final String value;
    private final String label;

    FuelType(String value, String label) {
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
    public static FuelType fromValue(String input) {
        if (input == null) {
            throw new UnsupportedFuelException("Fuel is null");
        }

        String normalized = input.trim().toLowerCase();

        if (normalized.equals("other")
                || normalized.equals("unknown")
                || normalized.equals("i dont know")
                || normalized.equals("i don't know")
                || normalized.equals("dont-know-other")
                || normalized.equals("dont_know_other")
                || normalized.equals("unknown-other")
                || normalized.equals("unknown_other")) {
            return UNKNOWN_OTHER;
        }

        for (FuelType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new UnsupportedFuelException("Unsupported fuel: " + input);
    }
}
