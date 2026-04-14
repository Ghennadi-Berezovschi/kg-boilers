package com.kgboilers.model.boilerinstallation.enums;

public enum QuoteStep {

    START("/quote"),

    FUEL_TYPE("/quote/fuel-type"),

    PROPERTY_OWNERSHIP("/quote/property-ownership"),

    PROPERTY_TYPE("/quote/property-type"),

    BEDROOMS("/quote/bedrooms"),

    BOILER_TYPE("/quote/boiler-type"),

    BOILER_CONVERSION("/quote/boiler-conversion"),

    BOILER_POSITION("/quote/boiler-position"),

    BOILER_LOCATION("/quote/boiler-location"),

    BOILER_CONDITION("/quote/boiler-condition"),

    RELOCATION("/quote/relocation"),

    RELOCATION_DISTANCE("/quote/relocation-distance"),

    FLUE_TYPE("/quote/flue-type"),

    FLUE_LENGTH("/quote/flue-length"),

    FLUE_POSITION("/quote/flue-position"),

    FLUE_CLEARANCE("/quote/flue-clearance"),

    FLUE_PROPERTY_DISTANCE("/quote/flue-property-distance"),

    RADIATOR_COUNT("/quote/radiator-count"),

    BATH_SHOWER_COUNT("/quote/bath-shower-count"),

    SUMMARY("/quote/summary");

    private final String path;

    QuoteStep(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public QuoteStep previous() {
        return switch (this) {
            case START -> START;

            case FUEL_TYPE -> START;

            case PROPERTY_OWNERSHIP -> FUEL_TYPE;

            case PROPERTY_TYPE -> PROPERTY_OWNERSHIP;

            case BEDROOMS -> PROPERTY_TYPE;

            case BOILER_TYPE -> BEDROOMS;

            case BOILER_CONVERSION -> BOILER_TYPE;

            case BOILER_POSITION -> BOILER_CONVERSION;

            case BOILER_LOCATION -> BOILER_POSITION;

            case BOILER_CONDITION -> BOILER_LOCATION;

            case RELOCATION -> BOILER_CONDITION;

            case RELOCATION_DISTANCE -> RELOCATION;

            case FLUE_TYPE -> RELOCATION;

            case FLUE_LENGTH -> FLUE_TYPE;

            case FLUE_POSITION -> FLUE_LENGTH;

            case FLUE_CLEARANCE -> FLUE_POSITION;

            case FLUE_PROPERTY_DISTANCE -> FLUE_CLEARANCE;

            case RADIATOR_COUNT -> FLUE_PROPERTY_DISTANCE;

            case BATH_SHOWER_COUNT -> RADIATOR_COUNT;

            case SUMMARY -> BATH_SHOWER_COUNT;
        };
    }
}
