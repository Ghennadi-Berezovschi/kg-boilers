package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.exception.boilerinstallationquote.UnsupportedBedroomsException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedBoilerFloorLevelException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedBoilerLocationException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedBoilerMakeException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedFuelException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedMagneticFilterException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedOwnershipException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedPowerFlushException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedPropertyTypeException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedSlopedRoofPositionException;
import com.kgboilers.exception.boilerrepairquote.UnsupportedBoilerAgeException;
import com.kgboilers.exception.boilerrepairquote.UnsupportedBoilerPressureException;
import com.kgboilers.exception.boilerrepairquote.UnsupportedFaultCodeDetailsException;
import com.kgboilers.exception.boilerrepairquote.UnsupportedFaultCodeDisplayException;
import com.kgboilers.exception.boilerrepairquote.UnsupportedRepairProblemException;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.*;
import com.kgboilers.model.boilerrepair.enums.BoilerAge;
import com.kgboilers.model.boilerrepair.enums.BoilerPressureStatus;
import com.kgboilers.model.boilerrepair.enums.FaultCodeDisplayStatus;
import com.kgboilers.model.boilerrepair.enums.MagneticFilterStatus;
import com.kgboilers.model.boilerrepair.enums.PowerFlushStatus;
import com.kgboilers.model.boilerrepair.enums.RepairProblem;
import org.springframework.stereotype.Service;

@Service
public class QuoteWizardService {

    private static final String BOILER_REPAIR_SERVICE = "boiler-repair";

    // =========================
    // START
    // =========================

    public QuoteStep startWizard(QuoteSessionState state, String postcode) {
        state.setPostcode(postcode);
        state.setCurrentStep(QuoteStep.FUEL_TYPE);
        return QuoteStep.FUEL_TYPE;
    }

    // =========================
    // ACCESS CONTROL
    // =========================

    public boolean canAccessStep(QuoteSessionState state, QuoteStep step) {
        return canAccessStep(state, step, null);
    }

    public boolean canAccessStep(QuoteSessionState state, QuoteStep step, String service) {
        if (state == null) {
            return step == QuoteStep.START;
        }

        boolean skipBedrooms = shouldSkipBedrooms(service);
        boolean skipBoilerPosition = shouldSkipBoilerPosition(service);
        boolean skipRepairDetails = shouldSkipRepairDetails(service);

        return switch (step) {
            case START -> true;

            case FUEL_TYPE -> state.hasPostcode();

            case PROPERTY_OWNERSHIP -> state.hasFuel();

            case PROPERTY_TYPE -> state.hasOwnership();

            case BEDROOMS -> state.hasPropertyType() && !skipBedrooms;

            case BOILER_TYPE -> skipBedrooms
                    ? state.hasPropertyType()
                    : state.hasBedrooms();

            case BOILER_MAKE -> BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())
                    && state.hasBoilerType();

            case BOILER_AGE -> BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())
                    && state.hasBoilerMake();

            case BOILER_CONVERSION -> !BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())
                    && state.hasBoilerType()
                    && state.getBoilerType() == BoilerType.HEAT_ONLY;

            case BOILER_POSITION -> !skipBoilerPosition
                    && state.hasBoilerType()
                    && (state.getBoilerType() != BoilerType.HEAT_ONLY || state.hasHeatOnlyConversion());

            case BOILER_LOCATION -> {
                if (skipBoilerPosition) {
                    if (BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())) {
                        yield state.hasBoilerAge();
                    }

                    yield state.hasBoilerType()
                            && (state.getBoilerType() != BoilerType.HEAT_ONLY
                            || shouldSkipBoilerPosition(service)
                            || state.hasHeatOnlyConversion());
                }

                yield state.hasBoilerPosition();
            }

            case BOILER_FLOOR_LEVEL -> !skipRepairDetails && state.hasBoilerLocation();

            case BOILER_CONDITION -> !skipRepairDetails && state.hasBoilerFloorLevel();

            case RELOCATION -> !skipRepairDetails && state.hasBoilerCondition();

            case RELOCATION_DISTANCE -> !skipRepairDetails && state.getRelocation() == Relocation.YES;

            case FLUE_TYPE -> !skipRepairDetails && state.hasRelocation()
                    && (state.getRelocation() == Relocation.NO || state.hasRelocationDistance());

            case FLUE_SHAPE -> !skipRepairDetails
                    && state.getFlueType() == FlueType.HORIZONTAL;

            case FLUE_LENGTH -> !skipRepairDetails && state.hasCompleteFlueSelection();

            case SLOPED_ROOF_POSITION -> !skipRepairDetails
                    && state.hasFlueLength()
                    && state.getFlueType() == FlueType.VERTICAL
                    && state.getVerticalFlueType() == VerticalFlueType.SLOPED_ROOF;

            case FLUE_POSITION -> !skipRepairDetails && state.hasFlueLength()
                    && state.getFlueType() == FlueType.HORIZONTAL;

            case FLUE_CLEARANCE -> !skipRepairDetails && state.hasFluePosition()
                    && state.getFlueType() == FlueType.HORIZONTAL;

            case FLUE_PROPERTY_DISTANCE -> !skipRepairDetails && state.hasFlueClearance()
                    && state.getFlueType() == FlueType.HORIZONTAL;

            case RADIATOR_COUNT -> skipRepairDetails
                    ? state.hasBoilerLocation()
                    : state.hasFlueLength()
                    && (state.getVerticalFlueType() != VerticalFlueType.SLOPED_ROOF || state.hasSlopedRoofPosition())
                    && (state.getFlueType() != FlueType.HORIZONTAL
                    || (state.hasFluePosition() && state.hasFlueClearance() && state.hasFluePropertyDistance()));

            case POWER_FLUSH -> skipRepairDetails && state.hasRadiatorCount();

            case MAGNETIC_FILTER -> skipRepairDetails && state.hasPowerFlushStatus();

            case REPAIR_PROBLEM -> skipRepairDetails && state.hasMagneticFilterStatus();

            case BOILER_PRESSURE -> skipRepairDetails && state.hasRepairProblem();

            case FAULT_CODE_DISPLAY -> skipRepairDetails && state.hasBoilerPressureStatus();

            case FAULT_CODE_DETAILS -> skipRepairDetails
                    && state.hasBoilerPressureStatus()
                    && state.requiresFaultCodeDetails();

            case BATH_SHOWER_COUNT -> !skipRepairDetails && state.hasRadiatorCount();

            case SUMMARY -> skipRepairDetails
                    ? state.hasRadiatorCount()
                    && state.hasPowerFlushStatus()
                    && state.hasMagneticFilterStatus()
                    && state.hasRepairProblem()
                    && state.hasBoilerPressureStatus()
                    && state.hasFaultCodeDisplayStatus()
                    && (!state.requiresFaultCodeDetails() || state.hasFaultCodeDetails())
                    : state.hasRelocation()
                    && (state.getRelocation() == Relocation.NO || state.hasRelocationDistance())
                    && state.hasCompleteFlueSelection()
                    && state.hasFlueLength()
                    && (state.getVerticalFlueType() != VerticalFlueType.SLOPED_ROOF || state.hasSlopedRoofPosition())
                    && (state.getFlueType() != FlueType.HORIZONTAL || state.hasFluePosition())
                    && state.hasRadiatorCount()
                    && state.hasBathShowerCount()
                    && (state.getFlueType() != FlueType.HORIZONTAL
                    || (state.hasFlueClearance() && state.hasFluePropertyDistance()));

            case CONTACT -> isComplete(state, service);
        };
    }

    // =========================
    // UPDATE METHODS (FLOW)
    // =========================

    public QuoteStep updateFuel(QuoteSessionState state, FuelType selectedFuel) {
        return updateFuel(state, selectedFuel, null);
    }

    public QuoteStep updateFuel(QuoteSessionState state,
                                FuelType selectedFuel,
                                String service) {
        if (selectedFuel == null) {
            throw new UnsupportedFuelException("Fuel is required");
        }

        boolean electricAllowed = selectedFuel == FuelType.ELECTRIC;

        if (selectedFuel != FuelType.GAS && !electricAllowed) {
            throw new UnsupportedFuelException("Unsupported fuel: " + selectedFuel.getValue());
        }

        state.setFuel(selectedFuel);
        state.setCurrentStep(QuoteStep.PROPERTY_OWNERSHIP);
        return QuoteStep.PROPERTY_OWNERSHIP;
    }

    public QuoteStep updateOwnership(QuoteSessionState state, OwnershipType selectedOwnership) {
        if (selectedOwnership == null) {
            throw new UnsupportedOwnershipException("Ownership is required");
        }

        state.setOwnership(selectedOwnership);
        state.setCurrentStep(QuoteStep.PROPERTY_TYPE);
        return QuoteStep.PROPERTY_TYPE;
    }

    public QuoteStep updatePropertyType(QuoteSessionState state, PropertyType selectedPropertyType) {
        return updatePropertyType(state, selectedPropertyType, null);
    }

    public QuoteStep updatePropertyType(QuoteSessionState state,
                                        PropertyType selectedPropertyType,
                                        String service) {
        if (selectedPropertyType == null) {
            throw new UnsupportedPropertyTypeException("Property type is required");
        }

        state.setPropertyType(selectedPropertyType);

        if (shouldSkipBedrooms(service)) {
            state.setBedrooms(null);
            state.setCurrentStep(QuoteStep.BOILER_TYPE);
            return QuoteStep.BOILER_TYPE;
        }

        state.setCurrentStep(QuoteStep.BEDROOMS);
        return QuoteStep.BEDROOMS;
    }

    public QuoteStep updateBedrooms(QuoteSessionState state, Bedrooms bedrooms) {
        if (bedrooms == null) {
            throw new UnsupportedBedroomsException("Unsupported bedrooms value: null");
        }

        state.setBedrooms(bedrooms);
        state.setCurrentStep(QuoteStep.BOILER_TYPE);
        return QuoteStep.BOILER_TYPE;
    }

    public QuoteStep updateBoilerType(QuoteSessionState state, BoilerType selectedBoilerType) {
        return updateBoilerType(state, selectedBoilerType, null);
    }

    public QuoteStep updateBoilerType(QuoteSessionState state,
                                      BoilerType selectedBoilerType,
                                      String service) {
        if (selectedBoilerType == null) {
            throw new IllegalArgumentException("Boiler type is required");
        }

        state.setBoilerType(selectedBoilerType);
        state.setBoilerMake(null);
        state.setBoilerAge(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);

        if (BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())) {
            state.setHeatOnlyConversion(null);
            state.setBoilerPosition(null);
            state.setRadiatorCount(null);
            state.setCurrentStep(QuoteStep.BOILER_MAKE);
            return QuoteStep.BOILER_MAKE;
        }

        if (selectedBoilerType == BoilerType.HEAT_ONLY) {
            state.setCurrentStep(QuoteStep.BOILER_CONVERSION);
            return QuoteStep.BOILER_CONVERSION;
        }

        if (shouldSkipBoilerPosition(service)) {
            state.setBoilerPosition(null);
            state.setCurrentStep(QuoteStep.BOILER_LOCATION);
            return QuoteStep.BOILER_LOCATION;
        }

        state.setCurrentStep(QuoteStep.BOILER_POSITION);
        return QuoteStep.BOILER_POSITION;
    }

    public QuoteStep updateBoilerMake(QuoteSessionState state, BoilerMake selectedBoilerMake, String service) {
        if (selectedBoilerMake == null) {
            throw new UnsupportedBoilerMakeException("Boiler make is required");
        }

        state.setBoilerMake(selectedBoilerMake);
        state.setBoilerAge(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);

        if (BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())) {
            state.setBoilerLocation(null);
            state.setRadiatorCount(null);
            state.setCurrentStep(QuoteStep.BOILER_AGE);
            return QuoteStep.BOILER_AGE;
        }

        state.setCurrentStep(QuoteStep.BOILER_POSITION);
        return QuoteStep.BOILER_POSITION;
    }

    public QuoteStep updateBoilerAge(QuoteSessionState state,
                                     BoilerAge boilerAge,
                                     String service) {
        if (!BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())) {
            throw new UnsupportedBoilerAgeException("Boiler age is only supported for boiler repair");
        }

        if (boilerAge == null) {
            throw new UnsupportedBoilerAgeException("Boiler age is required");
        }

        state.setBoilerAge(boilerAge);
        state.setBoilerLocation(null);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.BOILER_LOCATION);
        return QuoteStep.BOILER_LOCATION;
    }

    public QuoteStep updateBoilerConversion(QuoteSessionState state, HeatOnlyConversion conversion) {
        return updateBoilerConversion(state, conversion, null);
    }

    public QuoteStep updateBoilerConversion(QuoteSessionState state,
                                            HeatOnlyConversion conversion,
                                            String service) {
        if (conversion == null) {
            throw new IllegalArgumentException("Conversion choice is required");
        }

        state.setHeatOnlyConversion(conversion);

        if (shouldSkipBoilerPosition(service)) {
            state.setBoilerPosition(null);
            state.setCurrentStep(QuoteStep.BOILER_LOCATION);
            return QuoteStep.BOILER_LOCATION;
        }

        state.setCurrentStep(QuoteStep.BOILER_POSITION);
        return QuoteStep.BOILER_POSITION;
    }

    public QuoteStep updateBoilerPosition(QuoteSessionState state, BoilerPosition selectedPosition) {
        if (selectedPosition == null) {
            throw new IllegalArgumentException("Boiler position is required");
        }

        state.setBoilerPosition(selectedPosition);
        state.setCurrentStep(QuoteStep.BOILER_LOCATION);
        return QuoteStep.BOILER_LOCATION;
    }

    public QuoteStep updateBoilerLocation(QuoteSessionState state, BoilerLocation selectedLocation) {
        return updateBoilerLocation(state, selectedLocation, null);
    }

    public QuoteStep updateBoilerLocation(QuoteSessionState state,
                                          BoilerLocation selectedLocation,
                                          String service) {
        if (selectedLocation == null) {
            throw new UnsupportedBoilerLocationException("Boiler location is required");
        }

        state.setBoilerLocation(selectedLocation);
        if (shouldSkipRepairDetails(service)) {
            state.setBoilerFloorLevel(null);
            state.setRadiatorCount(null);
            state.setPowerFlushStatus(null);
            state.setMagneticFilterStatus(null);
            state.setRepairProblem(null);
            state.setBoilerPressureStatus(null);
            state.setFaultCodeDisplayStatus(null);
            state.setFaultCodeDetails(null);
            state.setCurrentStep(QuoteStep.RADIATOR_COUNT);
            return QuoteStep.RADIATOR_COUNT;
        }
        state.setCurrentStep(QuoteStep.BOILER_FLOOR_LEVEL);
        return QuoteStep.BOILER_FLOOR_LEVEL;
    }

    public QuoteStep updateBoilerFloorLevel(QuoteSessionState state, BoilerFloorLevel selectedFloorLevel) {
        return updateBoilerFloorLevel(state, selectedFloorLevel, null);
    }

    public QuoteStep updateBoilerFloorLevel(QuoteSessionState state,
                                            BoilerFloorLevel selectedFloorLevel,
                                            String service) {
        if (selectedFloorLevel == null) {
            throw new UnsupportedBoilerFloorLevelException("Boiler floor level is required");
        }

        state.setBoilerFloorLevel(selectedFloorLevel);

        if (shouldSkipRepairDetails(service)) {
            state.setBoilerCondition(null);
            state.setRelocation(null);
            state.setRelocationDistance(null);
            state.setFlueType(null);
            state.setVerticalFlueType(null);
            state.setHorizontalFlueShape(null);
            state.setFlueLength(null);
            state.setFluePosition(null);
            state.setFlueClearance(null);
            state.setFluePropertyDistance(null);
            state.setBathShowerCount(null);
            state.setCurrentStep(QuoteStep.RADIATOR_COUNT);
            return QuoteStep.RADIATOR_COUNT;
        }

        state.setCurrentStep(QuoteStep.BOILER_CONDITION);
        return QuoteStep.BOILER_CONDITION;
    }

    public QuoteStep updateBoilerCondition(QuoteSessionState state, BoilerCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Boiler condition is required");
        }

        state.setBoilerCondition(condition);
        state.setCurrentStep(QuoteStep.RELOCATION);
        return QuoteStep.RELOCATION;
    }

    public QuoteStep updateRelocation(QuoteSessionState state, Relocation relocation) {
        if (relocation == null) {
            throw new IllegalArgumentException("Relocation is required");
        }

        state.setRelocation(relocation);
        state.setFlueType(null);
        state.setVerticalFlueType(null);
        state.setHorizontalFlueShape(null);
        state.setFlueLength(null);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);

        if (relocation == Relocation.YES) {
            state.setCurrentStep(QuoteStep.RELOCATION_DISTANCE);
            return QuoteStep.RELOCATION_DISTANCE;
        }

        state.setRelocationDistance(null);
        state.setCurrentStep(QuoteStep.FLUE_TYPE);
        return QuoteStep.FLUE_TYPE;
    }

    public QuoteStep updateRelocationDistance(QuoteSessionState state, RelocationDistance distance) {
        if (distance == null) {
            throw new IllegalArgumentException("Relocation distance is required");
        }

        state.setRelocationDistance(distance);
        state.setFlueType(null);
        state.setVerticalFlueType(null);
        state.setHorizontalFlueShape(null);
        state.setFlueLength(null);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.FLUE_TYPE);
        return QuoteStep.FLUE_TYPE;
    }

    public boolean isComplete(QuoteSessionState state, String service) {
        if (state == null) {
            return false;
        }

        if (shouldSkipRepairDetails(service)) {
            return state.hasPostcode()
                    && state.hasFuel()
                    && state.hasOwnership()
                    && state.hasPropertyType()
                    && (shouldSkipBedrooms(service) || state.hasBedrooms())
                    && state.hasBoilerType()
                    && state.hasBoilerMake()
                    && state.hasBoilerAge()
                    && (shouldSkipBoilerPosition(service) || state.hasBoilerPosition())
                    && state.hasBoilerLocation()
                    && state.hasRadiatorCount()
                    && state.hasPowerFlushStatus()
                    && state.hasMagneticFilterStatus()
                    && state.hasRepairProblem()
                    && state.hasBoilerPressureStatus()
                    && state.hasFaultCodeDisplayStatus()
                    && (!state.requiresFaultCodeDetails() || state.hasFaultCodeDetails());
        }

        return state.hasPostcode()
                && state.hasFuel()
                && state.hasOwnership()
                && state.hasPropertyType()
                && (shouldSkipBedrooms(service) || state.hasBedrooms())
                && state.hasBoilerType()
                && (shouldSkipBoilerPosition(service) || state.hasBoilerPosition())
                && state.hasBoilerLocation()
                && state.hasBoilerFloorLevel()
                && state.hasBoilerCondition()
                && state.hasRelocation()
                && (state.getRelocation() == Relocation.NO || state.hasRelocationDistance())
                && state.hasCompleteFlueSelection()
                && state.hasFlueLength()
                && (state.getFlueType() != FlueType.HORIZONTAL || state.hasFluePosition())
                && state.hasBathShowerCount()
                && state.hasRadiatorCount()
                && (state.getFlueType() != FlueType.HORIZONTAL
                || (state.hasFlueClearance() && state.hasFluePropertyDistance()));
    }

    private boolean shouldSkipBedrooms(String service) {
        return BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
    }

    private boolean shouldSkipBoilerPosition(String service) {
        return BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
    }

    private boolean shouldSkipRepairDetails(String service) {
        return BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
    }

    public QuoteStep updateFlueType(QuoteSessionState state, FlueType flueType) {
        return updateFlueType(state, flueType, null);
    }

    public QuoteStep updateFlueType(QuoteSessionState state,
                                    FlueType flueType,
                                    VerticalFlueType verticalFlueType) {
        if (flueType == null) {
            throw new IllegalArgumentException("Flue type is required");
        }

        if (flueType == FlueType.VERTICAL && verticalFlueType == null) {
            throw new IllegalArgumentException("Vertical flue type is required");
        }

        state.setFlueType(flueType);
        state.setVerticalFlueType(flueType == FlueType.VERTICAL ? verticalFlueType : null);
        state.setHorizontalFlueShape(null);
        state.setFlueLength(null);
        state.setSlopedRoofPosition(null);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);

        if (flueType == FlueType.HORIZONTAL) {
            state.setCurrentStep(QuoteStep.FLUE_SHAPE);
            return QuoteStep.FLUE_SHAPE;
        }

        state.setCurrentStep(QuoteStep.FLUE_LENGTH);
        return QuoteStep.FLUE_LENGTH;
    }

    public QuoteStep updateHorizontalFlueShape(QuoteSessionState state, HorizontalFlueShape flueShape) {
        if (flueShape == null) {
            throw new IllegalArgumentException("Flue shape is required");
        }

        state.setHorizontalFlueShape(flueShape);
        state.setFlueLength(null);
        state.setSlopedRoofPosition(null);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.FLUE_LENGTH);
        return QuoteStep.FLUE_LENGTH;
    }

    public QuoteStep updateFlueLength(QuoteSessionState state, FlueLength flueLength) {
        if (flueLength == null) {
            throw new IllegalArgumentException("Flue length is required");
        }

        state.setFlueLength(flueLength);
        state.setSlopedRoofPosition(null);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);

        if (state.getFlueType() == FlueType.HORIZONTAL) {
            state.setCurrentStep(QuoteStep.FLUE_POSITION);
            return QuoteStep.FLUE_POSITION;
        }

        if (state.getVerticalFlueType() == VerticalFlueType.SLOPED_ROOF) {
            state.setCurrentStep(QuoteStep.SLOPED_ROOF_POSITION);
            return QuoteStep.SLOPED_ROOF_POSITION;
        }

        state.setCurrentStep(QuoteStep.RADIATOR_COUNT);
        return QuoteStep.RADIATOR_COUNT;
    }

    public QuoteStep updateSlopedRoofPosition(QuoteSessionState state, SlopedRoofPosition slopedRoofPosition) {
        if (slopedRoofPosition == null) {
            throw new UnsupportedSlopedRoofPositionException("Sloped roof position is required");
        }

        state.setSlopedRoofPosition(slopedRoofPosition);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.RADIATOR_COUNT);
        return QuoteStep.RADIATOR_COUNT;
    }

    public QuoteStep updateFluePosition(QuoteSessionState state, FluePosition fluePosition) {
        if (fluePosition == null) {
            throw new IllegalArgumentException("Flue position is required");
        }

        state.setFluePosition(fluePosition);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);

        if (state.getFlueType() == FlueType.HORIZONTAL) {
            state.setCurrentStep(QuoteStep.FLUE_CLEARANCE);
            return QuoteStep.FLUE_CLEARANCE;
        }

        state.setCurrentStep(QuoteStep.RADIATOR_COUNT);
        return QuoteStep.RADIATOR_COUNT;
    }

    public QuoteStep updateFlueClearance(QuoteSessionState state, FlueClearance flueClearance) {
        if (flueClearance == null) {
            throw new IllegalArgumentException("Flue clearance is required");
        }

        state.setFlueClearance(flueClearance);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.FLUE_PROPERTY_DISTANCE);
        return QuoteStep.FLUE_PROPERTY_DISTANCE;
    }

    public QuoteStep updateFluePropertyDistance(QuoteSessionState state,
                                                FluePropertyDistance fluePropertyDistance) {
        if (fluePropertyDistance == null) {
            throw new IllegalArgumentException("Flue property distance is required");
        }

        state.setFluePropertyDistance(fluePropertyDistance);
        state.setRadiatorCount(null);
        state.setPowerFlushStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.RADIATOR_COUNT);
        return QuoteStep.RADIATOR_COUNT;
    }

    public QuoteStep updateRadiatorCount(QuoteSessionState state, RadiatorCount radiatorCount) {
        return updateRadiatorCount(state, radiatorCount, null);
    }

    public QuoteStep updateRadiatorCount(QuoteSessionState state,
                                         RadiatorCount radiatorCount,
                                         String service) {
        if (radiatorCount == null) {
            throw new IllegalArgumentException("Radiator count is required");
        }

        state.setRadiatorCount(radiatorCount);
        state.setPowerFlushStatus(null);
        state.setMagneticFilterStatus(null);
        state.setBathShowerCount(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);

        if (shouldSkipRepairDetails(service)) {
            state.setCurrentStep(QuoteStep.POWER_FLUSH);
            return QuoteStep.POWER_FLUSH;
        }

        state.setCurrentStep(QuoteStep.BATH_SHOWER_COUNT);
        return QuoteStep.BATH_SHOWER_COUNT;
    }

    public QuoteStep updateBathShowerCount(QuoteSessionState state, BathShowerCount bathShowerCount) {
        if (bathShowerCount == null) {
            throw new IllegalArgumentException("Bath/shower count is required");
        }

        state.setBathShowerCount(bathShowerCount);
        state.setCurrentStep(QuoteStep.SUMMARY);
        return QuoteStep.SUMMARY;
    }

    public QuoteStep updatePowerFlush(QuoteSessionState state,
                                      PowerFlushStatus powerFlushStatus,
                                      String service) {
        if (!shouldSkipRepairDetails(service)) {
            throw new UnsupportedPowerFlushException("Power flush is only supported for boiler repair");
        }

        if (powerFlushStatus == null) {
            throw new UnsupportedPowerFlushException("Power flush answer is required");
        }

        state.setPowerFlushStatus(powerFlushStatus);
        state.setMagneticFilterStatus(null);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.MAGNETIC_FILTER);
        return QuoteStep.MAGNETIC_FILTER;
    }

    public QuoteStep updateMagneticFilter(QuoteSessionState state,
                                          MagneticFilterStatus magneticFilterStatus,
                                          String service) {
        if (!shouldSkipRepairDetails(service)) {
            throw new UnsupportedMagneticFilterException("Magnetic filter is only supported for boiler repair");
        }

        if (magneticFilterStatus == null) {
            throw new UnsupportedMagneticFilterException("Magnetic filter answer is required");
        }

        state.setMagneticFilterStatus(magneticFilterStatus);
        state.setRepairProblem(null);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.REPAIR_PROBLEM);
        return QuoteStep.REPAIR_PROBLEM;
    }

    public QuoteStep updateRepairProblem(QuoteSessionState state,
                                         RepairProblem repairProblem,
                                         String service) {
        if (!shouldSkipRepairDetails(service)) {
            throw new UnsupportedRepairProblemException("Repair problem is only supported for boiler repair");
        }

        if (repairProblem == null) {
            throw new UnsupportedRepairProblemException("Please choose what is not working");
        }

        state.setRepairProblem(repairProblem);
        state.setBoilerPressureStatus(null);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.BOILER_PRESSURE);
        return QuoteStep.BOILER_PRESSURE;
    }

    public QuoteStep updateBoilerPressure(QuoteSessionState state,
                                          BoilerPressureStatus boilerPressureStatus,
                                          String service) {
        if (!shouldSkipRepairDetails(service)) {
            throw new UnsupportedBoilerPressureException("Boiler pressure is only supported for boiler repair");
        }

        if (boilerPressureStatus == null) {
            throw new UnsupportedBoilerPressureException("Boiler pressure answer is required");
        }

        state.setBoilerPressureStatus(boilerPressureStatus);
        state.setFaultCodeDisplayStatus(null);
        state.setFaultCodeDetails(null);
        state.setCurrentStep(QuoteStep.FAULT_CODE_DISPLAY);
        return QuoteStep.FAULT_CODE_DISPLAY;
    }

    public QuoteStep updateFaultCodeDisplay(QuoteSessionState state,
                                            FaultCodeDisplayStatus faultCodeDisplayStatus,
                                            String service) {
        if (!shouldSkipRepairDetails(service)) {
            throw new UnsupportedFaultCodeDisplayException("Fault code question is only supported for boiler repair");
        }

        if (faultCodeDisplayStatus == null) {
            throw new UnsupportedFaultCodeDisplayException("Fault code answer is required");
        }

        state.setFaultCodeDisplayStatus(faultCodeDisplayStatus);
        state.setFaultCodeDetails(null);

        if (faultCodeDisplayStatus == FaultCodeDisplayStatus.YES_SHOWING) {
            state.setCurrentStep(QuoteStep.FAULT_CODE_DETAILS);
            return QuoteStep.FAULT_CODE_DETAILS;
        }

        state.setCurrentStep(QuoteStep.SUMMARY);
        return QuoteStep.SUMMARY;
    }

    public QuoteStep updateFaultCodeDetails(QuoteSessionState state,
                                            String faultCodeDetails,
                                            String service) {
        if (!shouldSkipRepairDetails(service)) {
            throw new UnsupportedFaultCodeDetailsException("Fault code details are only supported for boiler repair");
        }

        if (state.getFaultCodeDisplayStatus() != FaultCodeDisplayStatus.YES_SHOWING) {
            throw new UnsupportedFaultCodeDetailsException("Fault code details are only required when a fault code is showing");
        }

        if (faultCodeDetails == null || faultCodeDetails.isBlank()) {
            throw new UnsupportedFaultCodeDetailsException("Please provide the fault code or describe the signal shown");
        }

        String normalizedDetails = faultCodeDetails.trim();
        if (normalizedDetails.length() > 500) {
            throw new UnsupportedFaultCodeDetailsException("Please keep the fault code description under 500 characters");
        }

        state.setFaultCodeDetails(normalizedDetails);
        state.setCurrentStep(QuoteStep.SUMMARY);
        return QuoteStep.SUMMARY;
    }
}
