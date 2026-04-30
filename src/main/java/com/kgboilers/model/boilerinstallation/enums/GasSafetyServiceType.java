package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GasSafetyServiceType {
    BOILER_SERVICE("boiler-service", "Boiler service"),
    BOILER_SERVICE_AND_GAS_SAFETY_CERTIFICATE("boiler-service-and-gas-safety-certificate",
            "Boiler service and gas safety certificate for all gas appliances"),
    GAS_SAFETY_CERTIFICATE("gas-safety-certificate",
            "Gas safety certificate for all appliances"),
    SOMETHING_ELSE("something-else", "Something else");

    private final String value;
    private final String label;

    GasSafetyServiceType(String value, String label) {
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

    @JsonCreator
    public static GasSafetyServiceType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Service type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (GasSafetyServiceType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported service type: " + input);
    }
}
