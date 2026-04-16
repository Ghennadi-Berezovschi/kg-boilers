package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallationquote.QuoteProgressStageView;
import com.kgboilers.model.boilerinstallationquote.QuoteProgressView;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteProgressService {

    private static final List<String> STAGE_LABELS = List.of(
            "1.Your home",
            "2.Options",
            "3.Details",
            "4.Booking"
    );

    public QuoteProgressView buildProgress(QuoteSessionState state, QuoteStep currentStep, boolean bookingComplete) {
        List<QuoteStep> flow = buildVisibleFlow(state, currentStep);
        int totalSteps = Math.max(1, flow.size() - 1);
        int currentIndex = Math.max(0, flow.indexOf(currentStep));
        int currentStepNumber = Math.min(totalSteps, currentIndex);
        int percentComplete = (int) Math.round((currentStepNumber * 100.0) / totalSteps);
        int stepsRemaining = Math.max(0, totalSteps - currentStepNumber);

        int currentStage = resolveCurrentStage(currentStep, bookingComplete);
        List<QuoteProgressStageView> stages = buildStages(currentStage);

        return new QuoteProgressView(
                currentStepNumber,
                totalSteps,
                percentComplete,
                stepsRemaining,
                stages
        );
    }

    private List<QuoteStep> buildVisibleFlow(QuoteSessionState state, QuoteStep currentStep) {
        List<QuoteStep> flow = new ArrayList<>();
        flow.add(QuoteStep.START);
        flow.add(QuoteStep.FUEL_TYPE);
        flow.add(QuoteStep.PROPERTY_OWNERSHIP);
        flow.add(QuoteStep.PROPERTY_TYPE);
        flow.add(QuoteStep.BEDROOMS);
        flow.add(QuoteStep.BOILER_TYPE);

        if (shouldIncludeBoilerConversion(state, currentStep)) {
            flow.add(QuoteStep.BOILER_CONVERSION);
        }

        flow.add(QuoteStep.BOILER_POSITION);
        flow.add(QuoteStep.BOILER_LOCATION);
        flow.add(QuoteStep.BOILER_CONDITION);
        flow.add(QuoteStep.RELOCATION);

        if (shouldIncludeRelocationDistance(state, currentStep)) {
            flow.add(QuoteStep.RELOCATION_DISTANCE);
        }

        flow.add(QuoteStep.FLUE_TYPE);
        flow.add(QuoteStep.FLUE_LENGTH);

        if (shouldIncludeHorizontalFlueSteps(state, currentStep)) {
            flow.add(QuoteStep.FLUE_POSITION);
            flow.add(QuoteStep.FLUE_CLEARANCE);
            flow.add(QuoteStep.FLUE_PROPERTY_DISTANCE);
        }

        flow.add(QuoteStep.RADIATOR_COUNT);
        flow.add(QuoteStep.BATH_SHOWER_COUNT);
        flow.add(QuoteStep.SUMMARY);
        flow.add(QuoteStep.CONTACT);
        return flow;
    }

    private boolean shouldIncludeBoilerConversion(QuoteSessionState state, QuoteStep currentStep) {
        return currentStep == QuoteStep.BOILER_CONVERSION
                || (state != null && state.getBoilerType() == BoilerType.HEAT_ONLY);
    }

    private boolean shouldIncludeRelocationDistance(QuoteSessionState state, QuoteStep currentStep) {
        return currentStep == QuoteStep.RELOCATION_DISTANCE
                || (state != null && state.getRelocation() == Relocation.YES);
    }

    private boolean shouldIncludeHorizontalFlueSteps(QuoteSessionState state, QuoteStep currentStep) {
        return currentStep == QuoteStep.FLUE_POSITION
                || currentStep == QuoteStep.FLUE_CLEARANCE
                || currentStep == QuoteStep.FLUE_PROPERTY_DISTANCE
                || (state != null && state.getFlueType() == FlueType.HORIZONTAL);
    }

    private int resolveCurrentStage(QuoteStep currentStep, boolean bookingComplete) {
        if (bookingComplete) {
            return 4;
        }

        if (currentStep == QuoteStep.CONTACT) {
            return 3;
        }

        return switch (currentStep) {
            case RELOCATION,
                 RELOCATION_DISTANCE,
                 FLUE_TYPE,
                 FLUE_LENGTH,
                 FLUE_POSITION,
                 FLUE_CLEARANCE,
                 FLUE_PROPERTY_DISTANCE,
                 RADIATOR_COUNT,
                 BATH_SHOWER_COUNT,
                 SUMMARY -> 2;
            default -> 1;
        };
    }

    private List<QuoteProgressStageView> buildStages(int currentStage) {
        List<QuoteProgressStageView> stages = new ArrayList<>(STAGE_LABELS.size());

        for (int i = 0; i < STAGE_LABELS.size(); i++) {
            int stageNumber = i + 1;
            String state = "upcoming";
            if (stageNumber < currentStage) {
                state = "complete";
            } else if (stageNumber == currentStage) {
                state = "active";
            }

            stages.add(new QuoteProgressStageView(STAGE_LABELS.get(i), state));
        }

        return stages;
    }
}
