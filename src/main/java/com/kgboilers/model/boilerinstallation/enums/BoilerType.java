package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerType {

    COMBI("combi", "Combi boiler"),
    SYSTEM("system", "System boiler"),
    HEAT_ONLY("heat-only", "Heat-only boiler"),
    ELECTRIC("electric-boiler", "Electric boiler"),
    ELECTRIC_WITH_HOT_WATER_CYLINDER("electric-boiler-with-hot-water-cylinder", "Electric boiler with hot water cylinder"),
    OTHER("other", "I'm not sure");

    private final String value;
    private final String label;

    BoilerType(String value, String label) {
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
    public static BoilerType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Boiler type is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerType type : values()) {
            if (type.value.equals(normalized) || type.name().toLowerCase().equals(normalized)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported boiler type: " + input);
    }
}
