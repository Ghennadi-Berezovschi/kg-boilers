package com.kgboilers.model.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import lombok.Getter;

import java.util.List;

@Getter
public class BoilerRecommendationResult {

    private final BoilerType targetType;
    private final String targetTypeLabel;
    private final int requiredRadiators;
    private final int requiredBathShowerUnits;
    private final boolean exactMatch;
    private final List<BoilerModel> boilers;

    public BoilerRecommendationResult(BoilerType targetType,
                                      String targetTypeLabel,
                                      int requiredRadiators,
                                      int requiredBathShowerUnits,
                                      boolean exactMatch,
                                      List<BoilerModel> boilers) {
        this.targetType = targetType;
        this.targetTypeLabel = targetTypeLabel;
        this.requiredRadiators = requiredRadiators;
        this.requiredBathShowerUnits = requiredBathShowerUnits;
        this.exactMatch = exactMatch;
        this.boilers = boilers;
    }
}
