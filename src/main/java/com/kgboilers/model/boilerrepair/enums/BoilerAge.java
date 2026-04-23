package com.kgboilers.model.boilerrepair.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.kgboilers.exception.boilerrepairquote.UnsupportedBoilerAgeException;

public enum BoilerAge {

    UP_TO_TWO_YEARS("up-to-2-years", "Up to 2 years", "UP TO", "2"),
    TWO_TO_FIVE_YEARS("2-5-years", "2-5 years", null, "2-5"),
    FIVE_TO_TEN_YEARS("5-10-years", "5-10 years", null, "5-10"),
    OVER_TEN_YEARS("over-10-years", "Over 10 years", null, "10+"),
    DO_NOT_KNOW("do-not-know", "I don't know", null, "?");

    private final String value;
    private final String label;
    private final String headlinePrefix;
    private final String headlineValue;

    BoilerAge(String value, String label, String headlinePrefix, String headlineValue) {
        this.value = value;
        this.label = label;
        this.headlinePrefix = headlinePrefix;
        this.headlineValue = headlineValue;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getHeadlinePrefix() {
        return headlinePrefix;
    }

    public String getHeadlineValue() {
        return headlineValue;
    }

    @Override
    public String toString() {
        return label;
    }

    @JsonCreator
    public static BoilerAge fromValue(String input) {
        if (input == null) {
            throw new UnsupportedBoilerAgeException("Boiler age is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerAge age : values()) {
            if (age.value.equals(normalized)
                    || age.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return age;
            }
        }

        throw new UnsupportedBoilerAgeException("Unsupported boiler age: " + input);
    }
}
