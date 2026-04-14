package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedFuelException;

public enum FuelType {
    GAS("gas"),
    LPG("lpg"),
    OIL("oil"),
    ELECTRIC("electric");

    private final String value;

    FuelType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FuelType fromValue(String input) {
        if (input == null) {
            throw new UnsupportedFuelException("Fuel is null");
        }

        String normalized = input.trim().toLowerCase();

        for (FuelType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new UnsupportedFuelException("Unsupported fuel: " + input);
    }
}
