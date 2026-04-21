package com.kgboilers.service.centralheatingquote;

import com.kgboilers.model.boilerinstallationquote.QuoteProgressStageView;
import com.kgboilers.model.boilerinstallationquote.QuoteProgressView;
import com.kgboilers.model.centralheatingquote.CentralHeatingInstallationItem;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.InstallationPositionType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CentralHeatingQuoteProgressService {

    private static final List<String> STAGE_LABELS = List.of(
            "1.Your home",
            "2.Options",
            "3.Details",
            "4.Booking"
    );

    public QuoteProgressView buildProgress(CentralHeatingQuoteSessionState state,
                                           CentralHeatingQuoteStep currentStep,
                                           boolean bookingComplete) {
        List<CentralHeatingQuoteStep> flow = buildVisibleFlow(state, currentStep);
        int totalSteps = Math.max(1, flow.size() - 1);
        int currentIndex = Math.max(0, flow.indexOf(currentStep));
        int currentStepNumber = Math.min(totalSteps, currentIndex);
        int percentComplete = (int) Math.round((currentStepNumber * 100.0) / totalSteps);
        int stepsRemaining = Math.max(0, totalSteps - currentStepNumber);

        int currentStage = resolveCurrentStage(currentStep, bookingComplete);

        return new QuoteProgressView(
                currentStepNumber,
                totalSteps,
                percentComplete,
                stepsRemaining,
                buildStages(currentStage)
        );
    }

    private List<CentralHeatingQuoteStep> buildVisibleFlow(CentralHeatingQuoteSessionState state,
                                                           CentralHeatingQuoteStep currentStep) {
        List<CentralHeatingQuoteStep> flow = new ArrayList<>();
        flow.add(CentralHeatingQuoteStep.START);
        flow.add(CentralHeatingQuoteStep.PROPERTY_OWNERSHIP);
        flow.add(CentralHeatingQuoteStep.PROPERTY_TYPE);
        flow.add(CentralHeatingQuoteStep.BEDROOMS);
        flow.add(CentralHeatingQuoteStep.BOILER_TYPE);
        flow.add(CentralHeatingQuoteStep.FUEL_TYPE);
        flow.add(CentralHeatingQuoteStep.RADIATOR_COUNT);
        flow.add(CentralHeatingQuoteStep.TRV_VALVES);
        flow.add(CentralHeatingQuoteStep.POWER_FLUSH);
        flow.add(CentralHeatingQuoteStep.MAGNETIC_FILTER);
        flow.add(CentralHeatingQuoteStep.RADIATOR_ISSUES);

        if (shouldIncludeTrvInstallationQuantity(state, currentStep)) {
            flow.add(CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY);
        }

        if (shouldIncludeInstallationFlow(state, currentStep)) {
            flow.add(CentralHeatingQuoteStep.INSTALLATION_ITEM);
            flow.add(CentralHeatingQuoteStep.INSTALLATION_POSITION);

            if (shouldIncludeMoveDistance(state, currentStep)) {
                flow.add(CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE);
            }

            if (shouldIncludePipeDistance(state, currentStep)) {
                flow.add(CentralHeatingQuoteStep.INSTALLATION_PIPE_DISTANCE);
            }

            flow.add(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION);
            flow.add(CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION);
        }

        flow.add(CentralHeatingQuoteStep.SUMMARY);
        return flow;
    }

    private boolean shouldIncludeTrvInstallationQuantity(CentralHeatingQuoteSessionState state,
                                                         CentralHeatingQuoteStep currentStep) {
        return currentStep == CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY
                || (state != null && state.needsTrvInstallationQuantity());
    }

    private boolean shouldIncludeInstallationFlow(CentralHeatingQuoteSessionState state,
                                                  CentralHeatingQuoteStep currentStep) {
        return switch (currentStep) {
            case INSTALLATION_ITEM,
                 INSTALLATION_POSITION,
                 INSTALLATION_MOVE_DISTANCE,
                 INSTALLATION_PIPE_DISTANCE,
                 RADIATOR_SPECIFICATION,
                 ADD_ANOTHER_INSTALLATION -> true;
            default -> state != null
                    && (state.needsInstallationSpecification()
                    || state.hasInstallationItems()
                    || state.hasInstallationSpecification());
        };
    }

    private boolean shouldIncludeMoveDistance(CentralHeatingQuoteSessionState state,
                                              CentralHeatingQuoteStep currentStep) {
        return currentStep == CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE
                || hasInstallationPosition(state, InstallationPositionType.DIFFERENT_POSITION)
                || (state != null && state.getInstallationMoveDistance() != null);
    }

    private boolean shouldIncludePipeDistance(CentralHeatingQuoteSessionState state,
                                              CentralHeatingQuoteStep currentStep) {
        return currentStep == CentralHeatingQuoteStep.INSTALLATION_PIPE_DISTANCE
                || hasInstallationPosition(state, InstallationPositionType.NO_EXISTING_ITEM)
                || (state != null && state.getInstallationPipeDistance() != null);
    }

    private boolean hasInstallationPosition(CentralHeatingQuoteSessionState state,
                                            InstallationPositionType positionType) {
        if (state == null) {
            return false;
        }

        if (state.getInstallationPositionType() == positionType) {
            return true;
        }

        if (state.getInstallationItems() == null || state.getInstallationItems().isEmpty()) {
            return false;
        }

        for (CentralHeatingInstallationItem item : state.getInstallationItems()) {
            if (item.getInstallationPositionType() == positionType) {
                return true;
            }
        }

        return false;
    }

    private int resolveCurrentStage(CentralHeatingQuoteStep currentStep, boolean bookingComplete) {
        if (bookingComplete) {
            return 4;
        }

        return switch (currentStep) {
            case SUMMARY -> 3;
            case RADIATOR_COUNT,
                 TRV_VALVES,
                 POWER_FLUSH,
                 MAGNETIC_FILTER,
                 RADIATOR_ISSUES,
                 TRV_INSTALLATION_QUANTITY,
                 INSTALLATION_ITEM,
                 INSTALLATION_POSITION,
                 INSTALLATION_MOVE_DISTANCE,
                 INSTALLATION_PIPE_DISTANCE,
                 RADIATOR_SPECIFICATION,
                 ADD_ANOTHER_INSTALLATION -> 2;
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
