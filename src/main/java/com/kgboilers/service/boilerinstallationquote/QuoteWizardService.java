package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.exception.boilerinstallationquote.UnsupportedBedroomsException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedBoilerLocationException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedFuelException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedOwnershipException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedPropertyTypeException;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.*;
import org.springframework.stereotype.Service;

@Service
public class QuoteWizardService {

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
        if (state == null) {
            return step == QuoteStep.START;
        }

        return switch (step) {
            case START -> true;

            case FUEL_TYPE -> state.hasPostcode();

            case PROPERTY_OWNERSHIP -> state.hasFuel();

            case PROPERTY_TYPE -> state.hasOwnership();

            case BEDROOMS -> state.hasPropertyType();

            case BOILER_TYPE -> state.hasBedrooms();

            case BOILER_CONVERSION -> state.hasBoilerType()
                    && state.getBoilerType() == BoilerType.HEAT_ONLY;

            case BOILER_POSITION -> state.hasBoilerType()
                    && (state.getBoilerType() != BoilerType.HEAT_ONLY || state.hasHeatOnlyConversion());

            case BOILER_LOCATION -> state.hasBoilerPosition();

            case BOILER_CONDITION -> state.hasBoilerLocation();

            case RELOCATION -> state.hasBoilerCondition();

            case RELOCATION_DISTANCE -> state.getRelocation() == Relocation.YES;

            case FLUE_TYPE -> state.hasRelocation()
                    && (state.getRelocation() == Relocation.NO || state.hasRelocationDistance());

            case FLUE_LENGTH -> state.hasCompleteFlueSelection();

            case FLUE_POSITION -> state.hasFlueLength()
                    && state.getFlueType() == FlueType.HORIZONTAL;

            case FLUE_CLEARANCE -> state.hasFluePosition()
                    && state.getFlueType() == FlueType.HORIZONTAL;

            case FLUE_PROPERTY_DISTANCE -> state.hasFlueClearance()
                    && state.getFlueType() == FlueType.HORIZONTAL;

            case RADIATOR_COUNT -> state.hasFlueLength()
                    && (state.getFlueType() != FlueType.HORIZONTAL
                    || (state.hasFluePosition() && state.hasFlueClearance() && state.hasFluePropertyDistance()));

            case BATH_SHOWER_COUNT -> state.hasRadiatorCount();

            case SUMMARY -> state.hasRelocation()
                    && (state.getRelocation() == Relocation.NO || state.hasRelocationDistance())
                    && state.hasCompleteFlueSelection()
                    && state.hasFlueLength()
                    && (state.getFlueType() != FlueType.HORIZONTAL || state.hasFluePosition())
                    && state.hasRadiatorCount()
                    && state.hasBathShowerCount()
                    && (state.getFlueType() != FlueType.HORIZONTAL
                    || (state.hasFlueClearance() && state.hasFluePropertyDistance()));

            case CONTACT -> state.isComplete();
        };
    }

    // =========================
    // UPDATE METHODS (FLOW)
    // =========================

    public QuoteStep updateFuel(QuoteSessionState state, FuelType selectedFuel) {
        if (selectedFuel == null) {
            throw new UnsupportedFuelException("Fuel is required");
        }

        if (selectedFuel != FuelType.GAS) {
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
        if (selectedPropertyType == null) {
            throw new UnsupportedPropertyTypeException("Property type is required");
        }

        state.setPropertyType(selectedPropertyType);
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
        if (selectedBoilerType == null) {
            throw new IllegalArgumentException("Boiler type is required");
        }

        state.setBoilerType(selectedBoilerType);

        if (selectedBoilerType == BoilerType.HEAT_ONLY) {
            state.setCurrentStep(QuoteStep.BOILER_CONVERSION);
            return QuoteStep.BOILER_CONVERSION;
        }

        state.setCurrentStep(QuoteStep.BOILER_POSITION);
        return QuoteStep.BOILER_POSITION;
    }

    public QuoteStep updateBoilerConversion(QuoteSessionState state, HeatOnlyConversion conversion) {
        if (conversion == null) {
            throw new IllegalArgumentException("Conversion choice is required");
        }

        state.setHeatOnlyConversion(conversion);
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
        if (selectedLocation == null) {
            throw new UnsupportedBoilerLocationException("Boiler location is required");
        }

        state.setBoilerLocation(selectedLocation);
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
        state.setFlueLength(null);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setBathShowerCount(null);

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
        state.setFlueLength(null);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setBathShowerCount(null);
        state.setCurrentStep(QuoteStep.FLUE_TYPE);
        return QuoteStep.FLUE_TYPE;
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
        state.setFlueLength(null);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setBathShowerCount(null);
        state.setCurrentStep(QuoteStep.FLUE_LENGTH);
        return QuoteStep.FLUE_LENGTH;
    }

    public QuoteStep updateFlueLength(QuoteSessionState state, FlueLength flueLength) {
        if (flueLength == null) {
            throw new IllegalArgumentException("Flue length is required");
        }

        state.setFlueLength(flueLength);
        state.setFluePosition(null);
        state.setFlueClearance(null);
        state.setFluePropertyDistance(null);
        state.setRadiatorCount(null);
        state.setBathShowerCount(null);

        if (state.getFlueType() == FlueType.HORIZONTAL) {
            state.setCurrentStep(QuoteStep.FLUE_POSITION);
            return QuoteStep.FLUE_POSITION;
        }

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
        state.setBathShowerCount(null);

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
        state.setBathShowerCount(null);
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
        state.setBathShowerCount(null);
        state.setCurrentStep(QuoteStep.RADIATOR_COUNT);
        return QuoteStep.RADIATOR_COUNT;
    }

    public QuoteStep updateRadiatorCount(QuoteSessionState state, RadiatorCount radiatorCount) {
        if (radiatorCount == null) {
            throw new IllegalArgumentException("Radiator count is required");
        }

        state.setRadiatorCount(radiatorCount);
        state.setBathShowerCount(null);
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
}
