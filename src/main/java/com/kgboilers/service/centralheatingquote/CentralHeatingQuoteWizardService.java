package com.kgboilers.service.centralheatingquote;

import com.kgboilers.exception.boilerinstallationquote.UnsupportedBedroomsException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedBoilerTypeException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedFuelException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedOwnershipException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedMagneticFilterException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedPowerFlushException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedPropertyTypeException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedTrvValveException;
import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.MagneticFilterStatus;
import com.kgboilers.model.centralheatingquote.enums.PowerFlushStatus;
import com.kgboilers.model.centralheatingquote.enums.TrvValveStatus;
import org.springframework.stereotype.Service;

@Service
public class CentralHeatingQuoteWizardService {

    public CentralHeatingQuoteStep startWizard(CentralHeatingQuoteSessionState state, String postcode) {
        state.setPostcode(postcode);
        state.setCurrentStep(CentralHeatingQuoteStep.PROPERTY_OWNERSHIP);
        return CentralHeatingQuoteStep.PROPERTY_OWNERSHIP;
    }

    public boolean canAccessStep(CentralHeatingQuoteSessionState state, CentralHeatingQuoteStep step) {
        if (state == null) {
            return step == CentralHeatingQuoteStep.START;
        }

        return switch (step) {
            case START -> true;
            case PROPERTY_OWNERSHIP -> state.hasPostcode();
            case PROPERTY_TYPE -> state.hasOwnership();
            case BEDROOMS -> state.hasPropertyType();
            case BOILER_TYPE -> state.hasBedrooms();
            case FUEL_TYPE -> state.hasBoilerType();
            case RADIATOR_COUNT -> state.hasFuel();
            case TRV_VALVES -> state.hasRadiatorCount();
            case POWER_FLUSH -> state.hasTrvValveStatus();
            case MAGNETIC_FILTER -> state.hasPowerFlushStatus();
            case COMING_SOON -> state.hasMagneticFilterStatus();
        };
    }

    public CentralHeatingQuoteStep updateFuel(CentralHeatingQuoteSessionState state, FuelType selectedFuel) {
        if (selectedFuel == null) {
            throw new UnsupportedFuelException("Fuel is required");
        }

        state.setFuel(selectedFuel);
        state.setRadiatorCount(null);
        state.setCurrentStep(CentralHeatingQuoteStep.RADIATOR_COUNT);
        return CentralHeatingQuoteStep.RADIATOR_COUNT;
    }

    public CentralHeatingQuoteStep updateOwnership(CentralHeatingQuoteSessionState state, OwnershipType selectedOwnership) {
        if (selectedOwnership == null) {
            throw new UnsupportedOwnershipException("Ownership is required");
        }

        state.setOwnership(selectedOwnership);
        state.setCurrentStep(CentralHeatingQuoteStep.PROPERTY_TYPE);
        return CentralHeatingQuoteStep.PROPERTY_TYPE;
    }

    public CentralHeatingQuoteStep updatePropertyType(CentralHeatingQuoteSessionState state, PropertyType selectedPropertyType) {
        if (selectedPropertyType == null) {
            throw new UnsupportedPropertyTypeException("Property type is required");
        }

        state.setPropertyType(selectedPropertyType);
        state.setCurrentStep(CentralHeatingQuoteStep.BEDROOMS);
        return CentralHeatingQuoteStep.BEDROOMS;
    }

    public CentralHeatingQuoteStep updateBedrooms(CentralHeatingQuoteSessionState state, Bedrooms bedrooms) {
        if (bedrooms == null) {
            throw new UnsupportedBedroomsException("Unsupported bedrooms value: null");
        }

        state.setBedrooms(bedrooms);
        state.setCurrentStep(CentralHeatingQuoteStep.BOILER_TYPE);
        return CentralHeatingQuoteStep.BOILER_TYPE;
    }

    public CentralHeatingQuoteStep updateBoilerType(CentralHeatingQuoteSessionState state, BoilerType selectedBoilerType) {
        if (selectedBoilerType == null) {
            throw new UnsupportedBoilerTypeException("Boiler type is required");
        }

        if (selectedBoilerType == BoilerType.OTHER) {
            throw new UnsupportedBoilerTypeException("Unsupported boiler type: " + selectedBoilerType.getValue());
        }

        state.setBoilerType(selectedBoilerType);
        state.setCurrentStep(CentralHeatingQuoteStep.FUEL_TYPE);
        return CentralHeatingQuoteStep.FUEL_TYPE;
    }

    public CentralHeatingQuoteStep updateRadiatorCount(CentralHeatingQuoteSessionState state, RadiatorCount radiatorCount) {
        if (radiatorCount == null) {
            throw new IllegalArgumentException("Radiator count is required");
        }

        state.setRadiatorCount(radiatorCount);
        state.setTrvValveStatus(null);
        state.setCurrentStep(CentralHeatingQuoteStep.TRV_VALVES);
        return CentralHeatingQuoteStep.TRV_VALVES;
    }

    public CentralHeatingQuoteStep updateTrvValveStatus(CentralHeatingQuoteSessionState state, TrvValveStatus trvValveStatus) {
        if (trvValveStatus == null) {
            throw new UnsupportedTrvValveException("TRV valve answer is required");
        }

        state.setTrvValveStatus(trvValveStatus);
        state.setCurrentStep(CentralHeatingQuoteStep.POWER_FLUSH);
        return CentralHeatingQuoteStep.POWER_FLUSH;
    }

    public CentralHeatingQuoteStep updatePowerFlush(CentralHeatingQuoteSessionState state, PowerFlushStatus powerFlushStatus) {
        if (powerFlushStatus == null) {
            throw new UnsupportedPowerFlushException("Power flush answer is required");
        }

        state.setPowerFlushStatus(powerFlushStatus);
        state.setCurrentStep(CentralHeatingQuoteStep.MAGNETIC_FILTER);
        return CentralHeatingQuoteStep.MAGNETIC_FILTER;
    }

    public CentralHeatingQuoteStep updateMagneticFilter(CentralHeatingQuoteSessionState state,
                                                        MagneticFilterStatus magneticFilterStatus) {
        if (magneticFilterStatus == null) {
            throw new UnsupportedMagneticFilterException("Magnetic filter answer is required");
        }

        state.setMagneticFilterStatus(magneticFilterStatus);
        state.setCurrentStep(CentralHeatingQuoteStep.COMING_SOON);
        return CentralHeatingQuoteStep.COMING_SOON;
    }
}
