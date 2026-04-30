package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GasApplianceType {
    GAS_BOILER("gas-boiler", "Gas boiler"),
    GAS_COOKER("gas-cooker", "Gas cooker"),
    GAS_HOB("gas-hob", "Gas hob"),
    GAS_OVEN("gas-oven", "Gas oven"),
    GAS_FIRE("gas-fire", "Gas fire"),
    GAS_WATER_HEATER("gas-water-heater", "Gas water heater"),
    OTHER("other", "Other gas appliance");

    private final String value;
    private final String label;

    GasApplianceType(String value, String label) {
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
    public static GasApplianceType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Gas appliance type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (GasApplianceType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported gas appliance type: " + input);
    }
}
