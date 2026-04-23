package com.kgboilers.model.boilerrepair.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.kgboilers.exception.boilerrepairquote.UnsupportedRepairProblemException;
import lombok.Getter;

@Getter
public enum RepairProblem {

    HEATING("heating", "Heating"),
    HOT_WATER("hot-water", "Hot water"),
    HEATING_AND_HOT_WATER("heating-and-hot-water", "Heating & hot water"),
    SOMETHING_ELSE("something-else", "Something else");

    private final String value;
    private final String displayName;

    RepairProblem(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RepairProblem fromValue(String input) {
        if (input == null) {
            throw new UnsupportedRepairProblemException("Repair problem is null");
        }

        String normalized = input.trim().toLowerCase();

        for (RepairProblem repairProblem : values()) {
            if (repairProblem.value.equals(normalized)
                    || repairProblem.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return repairProblem;
            }
        }

        throw new UnsupportedRepairProblemException("Unsupported repair problem: " + input);
    }
}
