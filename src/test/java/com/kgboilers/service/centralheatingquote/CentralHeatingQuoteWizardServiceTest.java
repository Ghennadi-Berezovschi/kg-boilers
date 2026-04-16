package com.kgboilers.service.centralheatingquote;

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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CentralHeatingQuoteWizardServiceTest {

    private final CentralHeatingQuoteWizardService service = new CentralHeatingQuoteWizardService();

    @Test
    void startWizard_shouldGoToFuelType() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();

        CentralHeatingQuoteStep nextStep = service.startWizard(state, "E16 4JJ");

        assertEquals(CentralHeatingQuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertEquals("E16 4JJ", state.getPostcode());
        assertEquals(CentralHeatingQuoteStep.PROPERTY_OWNERSHIP, state.getCurrentStep());
    }

    @Test
    void updateSharedSteps_shouldEndAtComingSoon() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();

        assertEquals(CentralHeatingQuoteStep.PROPERTY_TYPE, service.updateOwnership(state, OwnershipType.HOMEOWNER));
        assertEquals(CentralHeatingQuoteStep.BEDROOMS, service.updatePropertyType(state, PropertyType.HOUSE));
        assertEquals(CentralHeatingQuoteStep.BOILER_TYPE, service.updateBedrooms(state, Bedrooms.THREE));
        assertEquals(CentralHeatingQuoteStep.FUEL_TYPE, service.updateBoilerType(state, BoilerType.COMBI));
        assertEquals(CentralHeatingQuoteStep.RADIATOR_COUNT, service.updateFuel(state, FuelType.GAS));
        assertEquals(CentralHeatingQuoteStep.TRV_VALVES, service.updateRadiatorCount(state, RadiatorCount.SIX_TO_NINE));
        assertEquals(CentralHeatingQuoteStep.POWER_FLUSH, service.updateTrvValveStatus(state, TrvValveStatus.NOT_ALL_OF_THEM));
        assertEquals(CentralHeatingQuoteStep.MAGNETIC_FILTER, service.updatePowerFlush(state, PowerFlushStatus.YES_DONE));
        assertEquals(CentralHeatingQuoteStep.COMING_SOON, service.updateMagneticFilter(state, MagneticFilterStatus.YES_HAS));
    }

    @Test
    void updateFuel_shouldAllowNonGasFuelTypes() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setBoilerType(BoilerType.COMBI);

        assertEquals(CentralHeatingQuoteStep.RADIATOR_COUNT, service.updateFuel(state, FuelType.LPG));
        assertEquals(FuelType.LPG, state.getFuel());

        assertEquals(CentralHeatingQuoteStep.RADIATOR_COUNT, service.updateFuel(state, FuelType.OIL));
        assertEquals(FuelType.OIL, state.getFuel());

        assertEquals(CentralHeatingQuoteStep.RADIATOR_COUNT, service.updateFuel(state, FuelType.ELECTRIC));
        assertEquals(FuelType.ELECTRIC, state.getFuel());
    }

    @Test
    void updateRadiatorCount_shouldRedirectToPowerFlush() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setFuel(FuelType.GAS);

        assertEquals(CentralHeatingQuoteStep.TRV_VALVES, service.updateRadiatorCount(state, RadiatorCount.TEN_TO_THIRTEEN));
        assertEquals(RadiatorCount.TEN_TO_THIRTEEN, state.getRadiatorCount());
        assertEquals("10-13 radiators", state.getRadiatorCountSummary());
    }

    @Test
    void updateTrvValveStatus_shouldRedirectToPowerFlush() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorCount(RadiatorCount.SIX_TO_NINE);

        assertEquals(CentralHeatingQuoteStep.POWER_FLUSH, service.updateTrvValveStatus(state, TrvValveStatus.NOT_ALL_OF_THEM));
        assertEquals(TrvValveStatus.NOT_ALL_OF_THEM, state.getTrvValveStatus());
        assertEquals("Not all of them", state.getTrvValveStatusSummary());
    }
}
