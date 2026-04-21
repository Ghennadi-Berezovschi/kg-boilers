package com.kgboilers.model.boilerinstallation.enums;

public enum QuoteStep {

    START("/quote"),

    FUEL_TYPE("/quote/fuel-type"),

    PROPERTY_OWNERSHIP("/quote/property-ownership"),

    PROPERTY_TYPE("/quote/property-type"),

    BEDROOMS("/quote/bedrooms"),

    BOILER_TYPE("/quote/boiler-type"),

    BOILER_MAKE("/quote/boiler-make"),

    BOILER_AGE("/quote/boiler-age"),

    BOILER_CONVERSION("/quote/boiler-conversion"),

    BOILER_POSITION("/quote/boiler-position"),

    BOILER_LOCATION("/quote/boiler-location"),

    BOILER_FLOOR_LEVEL("/quote/boiler-floor-level"),

    BOILER_CONDITION("/quote/boiler-condition"),

    RELOCATION("/quote/relocation"),

    RELOCATION_DISTANCE("/quote/relocation-distance"),

    FLUE_TYPE("/quote/flue-type"),

    FLUE_LENGTH("/quote/flue-length"),

    SLOPED_ROOF_POSITION("/quote/sloped-roof-position"),

    FLUE_POSITION("/quote/flue-position"),

    FLUE_CLEARANCE("/quote/flue-clearance"),

    FLUE_PROPERTY_DISTANCE("/quote/flue-property-distance"),

    RADIATOR_COUNT("/quote/radiator-count"),

    POWER_FLUSH("/quote/power-flush"),

    MAGNETIC_FILTER("/quote/magnetic-filter"),

    REPAIR_PROBLEM("/quote/repair-problem"),

    BOILER_PRESSURE("/quote/boiler-pressure"),

    FAULT_CODE_DISPLAY("/quote/fault-code"),

    FAULT_CODE_DETAILS("/quote/fault-code-details"),

    BATH_SHOWER_COUNT("/quote/bath-shower-count"),

    SUMMARY("/quote/summary"),

    CONTACT("/quote/contact");

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

            case BOILER_MAKE -> BOILER_TYPE;

            case BOILER_AGE -> BOILER_MAKE;

            case BOILER_CONVERSION -> BOILER_TYPE;

            case BOILER_POSITION -> BOILER_CONVERSION;

            case BOILER_LOCATION -> BOILER_POSITION;

            case BOILER_FLOOR_LEVEL -> BOILER_LOCATION;

            case BOILER_CONDITION -> BOILER_FLOOR_LEVEL;

            case RELOCATION -> BOILER_CONDITION;

            case RELOCATION_DISTANCE -> RELOCATION;

            case FLUE_TYPE -> RELOCATION;

            case FLUE_LENGTH -> FLUE_TYPE;

            case SLOPED_ROOF_POSITION -> FLUE_LENGTH;

            case FLUE_POSITION -> FLUE_LENGTH;

            case FLUE_CLEARANCE -> FLUE_POSITION;

            case FLUE_PROPERTY_DISTANCE -> FLUE_CLEARANCE;

            case RADIATOR_COUNT -> FLUE_PROPERTY_DISTANCE;

            case POWER_FLUSH -> RADIATOR_COUNT;

            case MAGNETIC_FILTER -> POWER_FLUSH;

            case REPAIR_PROBLEM -> MAGNETIC_FILTER;

            case BOILER_PRESSURE -> REPAIR_PROBLEM;

            case FAULT_CODE_DISPLAY -> BOILER_PRESSURE;

            case FAULT_CODE_DETAILS -> FAULT_CODE_DISPLAY;

            case BATH_SHOWER_COUNT -> RADIATOR_COUNT;

            case SUMMARY -> BATH_SHOWER_COUNT;

            case CONTACT -> SUMMARY;
        };
    }
}
