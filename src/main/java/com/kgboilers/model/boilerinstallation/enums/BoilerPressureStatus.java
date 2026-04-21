package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedBoilerPressureException;

public enum BoilerPressureStatus {

    YES_DROPPED_OR_DROPPING("yes-dropped-or-dropping", "Yes, it has dropped or is dropping"),
    NO_PRESSURE_NORMAL("no-pressure-normal", "No, the pressure is normal"),
    DO_NOT_KNOW("do-not-know", "I do not know");

    private final String value;
    private final String label;

    BoilerPressureStatus(String value, String label) {
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
    public static BoilerPressureStatus fromValue(String input) {
        if (input == null) {
            throw new UnsupportedBoilerPressureException("Boiler pressure answer is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerPressureStatus status : values()) {
            if (status.value.equals(normalized)
                    || status.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return status;
            }
        }

        throw new UnsupportedBoilerPressureException("Unsupported boiler pressure answer: " + input);
    }
}
