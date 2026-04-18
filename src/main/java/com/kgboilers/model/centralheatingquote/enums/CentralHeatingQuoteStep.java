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

    RADIATOR_ISSUES("/central-heating-quote/radiator-issues"),

    TRV_INSTALLATION_QUANTITY("/central-heating-quote/trv-installation-quantity"),

    INSTALLATION_ITEM("/central-heating-quote/installation-item"),

    INSTALLATION_POSITION("/central-heating-quote/installation-position"),

    INSTALLATION_MOVE_DISTANCE("/central-heating-quote/installation-move-distance"),

    INSTALLATION_PIPE_DISTANCE("/central-heating-quote/installation-pipe-distance"),

    RADIATOR_SPECIFICATION("/central-heating-quote/radiator-specification"),

    ADD_ANOTHER_INSTALLATION("/central-heating-quote/add-another-installation"),

    SUMMARY("/central-heating-quote/summary");

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
            case RADIATOR_ISSUES -> MAGNETIC_FILTER;
            case TRV_INSTALLATION_QUANTITY -> RADIATOR_ISSUES;
            case INSTALLATION_ITEM -> TRV_INSTALLATION_QUANTITY;
            case INSTALLATION_POSITION -> INSTALLATION_ITEM;
            case INSTALLATION_MOVE_DISTANCE -> INSTALLATION_POSITION;
            case INSTALLATION_PIPE_DISTANCE -> INSTALLATION_POSITION;
            case RADIATOR_SPECIFICATION -> INSTALLATION_PIPE_DISTANCE;
            case ADD_ANOTHER_INSTALLATION -> RADIATOR_SPECIFICATION;
            case SUMMARY -> ADD_ANOTHER_INSTALLATION;
        };
    }
}
