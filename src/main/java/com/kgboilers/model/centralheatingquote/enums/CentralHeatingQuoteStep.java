package com.kgboilers.model.centralheatingquote.enums;

public enum CentralHeatingQuoteStep {

    START("/central-heating-quote"),

    PROPERTY_OWNERSHIP("/central-heating-quote/property-ownership"),

    PROPERTY_TYPE("/central-heating-quote/property-type"),

    BEDROOMS("/central-heating-quote/bedrooms"),

    BOILER_TYPE("/central-heating-quote/boiler-type"),

    FUEL_TYPE("/central-heating-quote/fuel-type"),

    RADIATOR_COUNT("/central-heating-quote/radiator-count"),

    TRV_VALVES("/central-heating-quote/trv-valves"),

    POWER_FLUSH("/central-heating-quote/power-flush"),

    MAGNETIC_FILTER("/central-heating-quote/magnetic-filter"),

    COMING_SOON("/central-heating-quote/coming-soon");

    private final String path;

    CentralHeatingQuoteStep(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public CentralHeatingQuoteStep previous() {
        return switch (this) {
            case START -> START;
            case PROPERTY_OWNERSHIP -> START;
            case PROPERTY_TYPE -> PROPERTY_OWNERSHIP;
            case BEDROOMS -> PROPERTY_TYPE;
            case BOILER_TYPE -> BEDROOMS;
            case FUEL_TYPE -> BOILER_TYPE;
            case RADIATOR_COUNT -> FUEL_TYPE;
            case TRV_VALVES -> RADIATOR_COUNT;
            case POWER_FLUSH -> TRV_VALVES;
            case MAGNETIC_FILTER -> POWER_FLUSH;
            case COMING_SOON -> MAGNETIC_FILTER;
        };
    }
}
