package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallation.enums.VerticalFlueType;
import com.kgboilers.model.boilerinstallationquote.QuoteProgressStageView;
import com.kgboilers.model.boilerinstallationquote.QuoteProgressView;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteProgressService {

    private static final String BOILER_REPAIR_SERVICE = "boiler-repair";

    private static final List<String> STAGE_LABELS = List.of(
            "1.Your home",
            "2.Options",
            "3.Details",
            "4.Booking"
    );

    public QuoteProgressView buildProgress(QuoteSessionState state, QuoteStep currentStep, boolean bookingComplete) {
        return buildProgress(state, currentStep, bookingComplete, null);
    }

    public QuoteProgressView buildProgress(QuoteSessionState state,
                                           QuoteStep currentStep,
                                           boolean bookingComplete,
                                           String service) {
        List<QuoteStep> flow = buildVisibleFlow(state, currentStep, service);
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

    private List<QuoteStep> buildVisibleFlow(QuoteSessionState state, QuoteStep currentStep, String service) {
        boolean skipRepairDetails = shouldSkipRepairDetails(service);
        List<QuoteStep> flow = new ArrayList<>();
        flow.add(QuoteStep.START);
        flow.add(QuoteStep.FUEL_TYPE);
        flow.add(QuoteStep.PROPERTY_OWNERSHIP);
        flow.add(QuoteStep.PROPERTY_TYPE);
        if (!shouldSkipBedrooms(service)) {
            flow.add(QuoteStep.BEDROOMS);
        }
        flow.add(QuoteStep.BOILER_TYPE);
        if (BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())) {
            flow.add(QuoteStep.BOILER_MAKE);
            flow.add(QuoteStep.BOILER_AGE);
        }

        if (shouldIncludeBoilerConversion(state, currentStep, service)) {
            flow.add(QuoteStep.BOILER_CONVERSION);
        }

        if (!shouldSkipBoilerPosition(service)) {
            flow.add(QuoteStep.BOILER_POSITION);
        }
        flow.add(QuoteStep.BOILER_LOCATION);
        if (!skipRepairDetails) {
            flow.add(QuoteStep.BOILER_FLOOR_LEVEL);
        }

        if (!skipRepairDetails) {
            flow.add(QuoteStep.BOILER_CONDITION);
            flow.add(QuoteStep.RELOCATION);

            if (shouldIncludeRelocationDistance(state, currentStep)) {
                flow.add(QuoteStep.RELOCATION_DISTANCE);
            }

            flow.add(QuoteStep.FLUE_TYPE);
            if (shouldIncludeHorizontalFlueShape(state, currentStep)) {
                flow.add(QuoteStep.FLUE_SHAPE);
            }
            flow.add(QuoteStep.FLUE_LENGTH);

            if (shouldIncludeSlopedRoofPosition(state, currentStep)) {
                flow.add(QuoteStep.SLOPED_ROOF_POSITION);
            }

            if (shouldIncludeHorizontalFlueSteps(state, currentStep)) {
                flow.add(QuoteStep.FLUE_POSITION);
                flow.add(QuoteStep.FLUE_CLEARANCE);
                flow.add(QuoteStep.FLUE_PROPERTY_DISTANCE);
            }
        }

        flow.add(QuoteStep.RADIATOR_COUNT);

        if (skipRepairDetails) {
            flow.add(QuoteStep.POWER_FLUSH);
            flow.add(QuoteStep.MAGNETIC_FILTER);
            flow.add(QuoteStep.REPAIR_PROBLEM);
            flow.add(QuoteStep.BOILER_PRESSURE);
            flow.add(QuoteStep.FAULT_CODE_DISPLAY);
            if (shouldIncludeFaultCodeDetails(state, currentStep)) {
                flow.add(QuoteStep.FAULT_CODE_DETAILS);
            }
        }

        if (!skipRepairDetails) {
            flow.add(QuoteStep.BATH_SHOWER_COUNT);
        }

        flow.add(QuoteStep.SUMMARY);
        flow.add(QuoteStep.CONTACT);
        return flow;
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

    private boolean shouldIncludeBoilerConversion(QuoteSessionState state, QuoteStep currentStep, String service) {
        if (BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())) {
            return false;
        }

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

    private boolean shouldIncludeHorizontalFlueShape(QuoteSessionState state, QuoteStep currentStep) {
        return currentStep == QuoteStep.FLUE_SHAPE
                || (state != null && state.getFlueType() == FlueType.HORIZONTAL);
    }

    private boolean shouldIncludeSlopedRoofPosition(QuoteSessionState state, QuoteStep currentStep) {
        return currentStep == QuoteStep.SLOPED_ROOF_POSITION
                || (state != null
                && state.getFlueType() == FlueType.VERTICAL
                && state.getVerticalFlueType() == VerticalFlueType.SLOPED_ROOF);
    }

    private boolean shouldIncludeFaultCodeDetails(QuoteSessionState state, QuoteStep currentStep) {
        return currentStep == QuoteStep.FAULT_CODE_DETAILS
                || (state != null && state.requiresFaultCodeDetails());
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
                 FLUE_SHAPE,
                 FLUE_LENGTH,
                 SLOPED_ROOF_POSITION,
                 FLUE_POSITION,
                 FLUE_CLEARANCE,
                 FLUE_PROPERTY_DISTANCE,
                 RADIATOR_COUNT,
                 POWER_FLUSH,
                 MAGNETIC_FILTER,
                 REPAIR_PROBLEM,
                 BOILER_PRESSURE,
                 FAULT_CODE_DISPLAY,
                 FAULT_CODE_DETAILS,
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
