package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum AirConditioningRoomSize {

    UP_TO_15_SQM("up-to-15-sqm", "Up to 15 m²"),
    FROM_16_TO_25_SQM("16-25-sqm", "16-25 m²"),
    FROM_26_TO_35_SQM("26-35-sqm", "26-35 m²"),
    FROM_36_TO_50_SQM("36-50-sqm", "36-50 m²"),
    OVER_50_SQM("over-50-sqm", "Over 50 m²"),
    OTHER_NOT_SURE("other-not-sure", "Other / Not sure");

    private final String value;
    private final String label;

    AirConditioningRoomSize(String value, String label) {
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
    public static AirConditioningRoomSize fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Air conditioning room size is required");
        }

        String normalizedValue = value.trim();
        return Arrays.stream(values())
                .filter(size -> size.value.equalsIgnoreCase(normalizedValue)
                        || size.name().equalsIgnoreCase(normalizedValue.replace("-", "_")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported air conditioning room size: " + value));
    }
}
