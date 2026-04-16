package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
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
        assertEquals(15, progress.totalSteps());
        assertEquals(7, progress.percentComplete());
        assertEquals("active", progress.stages().get(0).state());
    }

    @Test
    void buildProgress_shouldIncludeConditionalStepsForHeatOnlyHorizontalFlow() {
        QuoteSessionState state = new QuoteSessionState();
        state.setBoilerType(BoilerType.HEAT_ONLY);
        state.setRelocation(Relocation.YES);
        state.setFlueType(FlueType.HORIZONTAL);

        QuoteProgressView progress = service.buildProgress(state, QuoteStep.FLUE_CLEARANCE, false);

        assertEquals(15, progress.currentStepNumber());
        assertEquals(20, progress.totalSteps());
        assertEquals(75, progress.percentComplete());
        assertEquals("active", progress.stages().get(1).state());
    }

    @Test
    void buildProgress_shouldMarkBookingStageWhenLeadIsSubmitted() {
        QuoteProgressView progress = service.buildProgress(new QuoteSessionState(), QuoteStep.CONTACT, true);

        assertEquals(100, progress.percentComplete());
        assertEquals("complete", progress.stages().get(2).state());
        assertEquals("active", progress.stages().get(3).state());
    }
}
