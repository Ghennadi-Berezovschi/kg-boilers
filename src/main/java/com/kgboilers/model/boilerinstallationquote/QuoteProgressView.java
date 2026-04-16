package com.kgboilers.model.boilerinstallationquote;

import java.util.List;

public record QuoteProgressView(
        int currentStepNumber,
        int totalSteps,
        int percentComplete,
        int stepsRemaining,
        List<QuoteProgressStageView> stages
) {
}
