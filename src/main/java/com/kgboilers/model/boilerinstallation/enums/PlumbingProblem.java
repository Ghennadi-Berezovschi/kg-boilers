package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PlumbingProblem {

    LEAKING_TAP("leaking-tap", "Leaking tap"),
    LEAKING_PIPE("leaking-pipe", "Leaking pipe"),
    BLOCKED_SINK("blocked-sink", "Blocked sink"),
    BLOCKED_TOILET("blocked-toilet", "Blocked toilet"),
    BLOCKED_BATH_SHOWER("blocked-bath-shower", "Blocked bath / shower"),
    TOILET_NOT_FLUSHING("toilet-not-flushing", "Toilet not flushing"),
    TOILET_CONSTANTLY_RUNNING("toilet-constantly-running", "Toilet constantly running"),
    LOW_WATER_PRESSURE("low-water-pressure", "No water / low water pressure"),
    TAP_REPLACEMENT("tap-replacement", "Tap replacement"),
    SHOWER_PROBLEM("shower-problem", "Shower problem"),
    WASTE_PIPE_PROBLEM("waste-pipe-problem", "Waste pipe problem"),
    APPLIANCE_CONNECTION("appliance-connection", "Washing machine / dishwasher connection"),
    RADIATOR_VALVE_LEAKING("radiator-valve-leaking", "Radiator valve leaking"),
    OUTSIDE_TAP_INSTALLATION("outside-tap-installation", "Outside tap installation"),
    GENERAL_PLUMBING_REPAIR("general-plumbing-repair", "General plumbing repair"),
    OTHER_PLUMBING_ISSUE("other-plumbing-issue", "Other plumbing issue");

    private final String value;
    private final String label;

    PlumbingProblem(String value, String label) {
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
    public static PlumbingProblem fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Plumbing problem is required");
        }

        String normalizedValue = value.trim();
        return Arrays.stream(values())
                .filter(problem -> problem.value.equalsIgnoreCase(normalizedValue)
                        || problem.name().equalsIgnoreCase(normalizedValue.replace("-", "_")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported plumbing problem: " + value));
    }
}
