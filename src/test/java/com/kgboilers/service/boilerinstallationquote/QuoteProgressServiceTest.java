package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.GasSafetyServiceType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallationquote.QuoteProgressView;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuoteProgressServiceTest {

    private final QuoteProgressService service = new QuoteProgressService();

    @Test
    void buildProgress_shouldShowFirstStepProgressForFuelPage() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");

        QuoteProgressView progress = service.buildProgress(state, QuoteStep.FUEL_TYPE, false);

        assertEquals(1, progress.currentStepNumber());
        assertEquals(16, progress.totalSteps());
        assertEquals(6, progress.percentComplete());
        assertEquals("active", progress.stages().get(0).state());
    }

    @Test
    void buildProgress_shouldIncludeConditionalStepsForHeatOnlyHorizontalFlow() {
        QuoteSessionState state = new QuoteSessionState();
        state.setBoilerType(BoilerType.HEAT_ONLY);
        state.setRelocation(Relocation.YES);
        state.setFlueType(FlueType.HORIZONTAL);

        QuoteProgressView progress = service.buildProgress(state, QuoteStep.FLUE_CLEARANCE, false);

        assertEquals(17, progress.currentStepNumber());
        assertEquals(22, progress.totalSteps());
        assertEquals(77, progress.percentComplete());
        assertEquals("active", progress.stages().get(1).state());
    }

    @Test
    void buildProgress_shouldMarkBookingStageWhenLeadIsSubmitted() {
        QuoteProgressView progress = service.buildProgress(new QuoteSessionState(), QuoteStep.SUMMARY, true);

        assertEquals(progress.totalSteps(), progress.currentStepNumber());
        assertEquals(100, progress.percentComplete());
        assertEquals(0, progress.stepsRemaining());
        assertEquals("complete", progress.stages().get(2).state());
        assertEquals("active", progress.stages().get(3).state());
    }

    @Test
    void buildProgress_shouldSkipHomeAndPropertyQuestionsForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setFuel(com.kgboilers.model.boilerinstallation.enums.FuelType.GAS);

        QuoteProgressView progress = service.buildProgress(state, QuoteStep.BOILER_TYPE, false, "boiler-repair");

        assertEquals(2, progress.currentStepNumber());
        assertEquals(8, progress.totalSteps());
    }

    @Test
    void buildProgress_shouldSkipBoilerConversionForHeatOnlyBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setFuel(com.kgboilers.model.boilerinstallation.enums.FuelType.GAS);
        state.setBoilerType(BoilerType.HEAT_ONLY);
        state.setBoilerMake(com.kgboilers.model.boilerinstallation.enums.BoilerMake.VAILLANT);

        QuoteProgressView progress = service.buildProgress(state, QuoteStep.BOILER_AGE, false, "boiler-repair");

        assertEquals(4, progress.currentStepNumber());
        assertEquals(8, progress.totalSteps());
    }

    @Test
    void buildProgress_shouldIncludeFaultCodeDetailsForBoilerRepairWhenNeeded() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setFuel(com.kgboilers.model.boilerinstallation.enums.FuelType.GAS);
        state.setBoilerType(BoilerType.COMBI);
        state.setBoilerMake(com.kgboilers.model.boilerinstallation.enums.BoilerMake.VAILLANT);
        state.setBoilerAge(com.kgboilers.model.boilerrepair.enums.BoilerAge.TWO_TO_FIVE_YEARS);
        state.setRepairProblem(com.kgboilers.model.boilerrepair.enums.RepairProblem.HEATING);
        state.setFaultCodeDisplayStatus(com.kgboilers.model.boilerrepair.enums.FaultCodeDisplayStatus.YES_SHOWING);

        QuoteProgressView progress = service.buildProgress(state, QuoteStep.FAULT_CODE_DETAILS, false, "boiler-repair");

        assertEquals(7, progress.currentStepNumber());
        assertEquals(9, progress.totalSteps());
    }

    @Test
    void buildProgress_shouldStopGasSafetyCertificateFlowAfterPropertyType() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setOwnership(com.kgboilers.model.boilerinstallation.enums.OwnershipType.HOMEOWNER);
        state.setPropertyType(com.kgboilers.model.boilerinstallation.enums.PropertyType.HOUSE);

        QuoteProgressView progress = service.buildProgress(state, QuoteStep.SUMMARY, false, "gas-safety-certificate");

        assertEquals(4, progress.currentStepNumber());
        assertEquals(5, progress.totalSteps());
        assertEquals(80, progress.percentComplete());
    }

    @Test
    void buildProgress_shouldIncludeBoilerTypeForBoilerServiceGasSafetyFlow() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setGasSafetyServiceType(GasSafetyServiceType.BOILER_SERVICE);

        QuoteProgressView progress = service.buildProgress(state, QuoteStep.BOILER_TYPE, false, "gas-safety-certificate");

        assertEquals(2, progress.currentStepNumber());
        assertEquals(7, progress.totalSteps());
        assertEquals(29, progress.percentComplete());
    }
}
