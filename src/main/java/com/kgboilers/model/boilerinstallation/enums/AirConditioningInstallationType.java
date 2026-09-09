package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum AirConditioningInstallationType {

    BEDROOM_INSTALLATION("bedroom-installation", "Bedroom installation"),
    LIVING_ROOM_INSTALLATION("living-room-installation", "Living room installation"),
    KITCHEN_INSTALLATION("kitchen-installation", "Kitchen installation"),
    HOME_OFFICE_INSTALLATION("home-office-installation", "Home office installation"),
    REPLACEMENT_UNIT("replacement-unit", "Replace existing air conditioning unit"),
    NOT_SURE("not-sure", "Not sure / Other");

    private final String value;
    private final String label;

    AirConditioningInstallationType(String value, String label) {
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
    public static AirConditioningInstallationType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Air conditioning installation type is required");
        }

        String normalizedValue = value.trim();
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(normalizedValue)
                        || type.name().equalsIgnoreCase(normalizedValue.replace("-", "_")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported air conditioning installation type: " + value));
    }
}
