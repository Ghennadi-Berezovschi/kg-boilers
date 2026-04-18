package com.kgboilers.model.centralheatingquote.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RadiatorIssueType {

    CLEAN_HEATING_SYSTEM_WITH_CHEMICAL("clean-heating-system-with-chemical", "Clean your heating system with a chemical"),
    INSTALL_RADIATOR_OR_TOWEL_RAIL("install-radiator-or-towel-rail", "Install a radiator or towel rail"),
    INSTALL_TRV_VALVES("install-trv-valves", "Install TRV valves, Lockshield valves, Towel rail valves"),
    ALL_RADIATORS_ARE_COLD("all-radiators-are-cold", "All radiators are cold"),
    NOT_HEATING_PROPERLY("not-heating-properly", "Radiator is not heating properly"),
    RADIATOR_LEAK("radiator-leak", "Radiator leak"),
    RADIATOR_PIPE_LEAK("radiator-pipe-leak", "Radiator pipe leak"),
    RADIATOR_VALVE_ISSUE("radiator-valve-issue", "Radiator valve issue"),
    SOMETHING_ELSE("something-else", "Something else");

    private final String value;
    private final String label;

    RadiatorIssueType(String value, String label) {
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
    public static RadiatorIssueType fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Radiator issue is null");
        }

        String normalized = input.trim().toLowerCase();

        for (RadiatorIssueType issueType : values()) {
            if (issueType.value.equals(normalized) || issueType.name().equalsIgnoreCase(normalized)) {
                return issueType;
            }
        }

        throw new IllegalArgumentException("Unsupported radiator issue: " + input);
    }
}
