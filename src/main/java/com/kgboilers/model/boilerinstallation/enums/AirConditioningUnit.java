package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum AirConditioningUnit {

    DAIKIN_SENSIRA_2_5("daikin-sensira-2-5", "Daikin", "Sensira 2.5 kW wall-mounted unit", "2.5 kW", "Up to 15 m²"),
    DAIKIN_SENSIRA_3_5("daikin-sensira-3-5", "Daikin", "Sensira 3.5 kW wall-mounted unit", "3.5 kW", "Up to 28 m²"),
    DAIKIN_SENSIRA_5_0("daikin-sensira-5-0", "Daikin", "Sensira 5.0 kW wall-mounted unit", "5.0 kW", "Up to 40 m²"),
    FUJITSU_STANDARD_2_5("fujitsu-standard-2-5", "Fujitsu", "Standard 2.5 kW ASEH09KMCG", "2.5 kW", "Up to 15 m²"),
    FUJITSU_STANDARD_3_4("fujitsu-standard-3-4", "Fujitsu", "Standard 3.4 kW ASEH12KMCG", "3.4 kW", "Up to 28 m²"),
    FUJITSU_STANDARD_5_2("fujitsu-standard-5-2", "Fujitsu", "Standard 5.2 kW ASYG18KMTE", "5.2 kW", "Up to 40 m²");

    private final String value;
    private final String brand;
    private final String model;
    private final String coolingCapacity;
    private final String roomCoverage;

    AirConditioningUnit(String value, String brand, String model, String coolingCapacity, String roomCoverage) {
        this.value = value;
        this.brand = brand;
        this.model = model;
        this.coolingCapacity = coolingCapacity;
        this.roomCoverage = roomCoverage;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getCoolingCapacity() {
        return coolingCapacity;
    }

    public String getRoomCoverage() {
        return roomCoverage;
    }

    public String getLabel() {
        return brand + " " + model;
    }

    public String getSummary() {
        return getLabel() + " (" + coolingCapacity + ", " + roomCoverage + ")";
    }

    @JsonCreator
    public static AirConditioningUnit fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(unit -> unit.value.equalsIgnoreCase(value.trim())
                        || unit.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported air conditioning unit: " + value));
    }
}
