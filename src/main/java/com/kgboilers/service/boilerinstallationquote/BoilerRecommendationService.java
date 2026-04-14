package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.HeatOnlyConversion;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class BoilerRecommendationService {

    private static final int RADIATOR_WEIGHT = 3;
    private static final int BATH_SHOWER_WEIGHT = 10;
    private static final int MAX_RECOMMENDATIONS = 3;

    private final BoilerCatalogService boilerCatalogService;

    public BoilerRecommendationService(BoilerCatalogService boilerCatalogService) {
        this.boilerCatalogService = boilerCatalogService;
    }

    public BoilerRecommendationResult recommend(QuoteSessionState state) {
        BoilerType targetType = resolveTargetType(state);
        int requiredRadiators = toRequiredRadiators(state != null ? state.getRadiatorCount() : null);
        int requiredBathShowerUnits = toRequiredBathShowerUnits(state != null ? state.getBathShowerCount() : null);

        if (targetType == null || requiredRadiators == 0 || requiredBathShowerUnits == 0) {
            return new BoilerRecommendationResult(
                    targetType,
                    toTargetTypeLabel(targetType),
                    requiredRadiators,
                    requiredBathShowerUnits,
                    false,
                    List.of()
            );
        }

        List<BoilerModel> catalog = boilerCatalogService.getBoilersForType(targetType).stream()
                .filter(BoilerModel::isEnabled)
                .toList();

        List<BoilerModel> exactMatches = catalog.stream()
                .filter(boiler -> matchesExactly(boiler, requiredRadiators, requiredBathShowerUnits))
                .sorted(exactMatchComparator(requiredRadiators, requiredBathShowerUnits))
                .limit(MAX_RECOMMENDATIONS)
                .toList();

        if (!exactMatches.isEmpty()) {
            return new BoilerRecommendationResult(
                    targetType,
                    toTargetTypeLabel(targetType),
                    requiredRadiators,
                    requiredBathShowerUnits,
                    true,
                    exactMatches
            );
        }

        List<BoilerModel> closestMatches = catalog.stream()
                .sorted(closestMatchComparator(requiredRadiators, requiredBathShowerUnits))
                .limit(MAX_RECOMMENDATIONS)
                .toList();

        return new BoilerRecommendationResult(
                targetType,
                toTargetTypeLabel(targetType),
                requiredRadiators,
                requiredBathShowerUnits,
                false,
                closestMatches
        );
    }

    private BoilerType resolveTargetType(QuoteSessionState state) {
        if (state == null || state.getBoilerType() == null) {
            return null;
        }

        if (state.getBoilerType() == BoilerType.HEAT_ONLY
                && state.getHeatOnlyConversion() == HeatOnlyConversion.YES) {
            return BoilerType.COMBI;
        }

        return state.getBoilerType();
    }

    private int toRequiredRadiators(RadiatorCount radiatorCount) {
        if (radiatorCount == null) {
            return 0;
        }

        return switch (radiatorCount) {
            case ZERO_TO_FIVE -> 5;
            case SIX_TO_NINE -> 9;
            case TEN_TO_THIRTEEN -> 13;
            case FOURTEEN_TO_SIXTEEN -> 16;
            case SEVENTEEN_PLUS -> 17;
        };
    }

    private int toRequiredBathShowerUnits(BathShowerCount bathShowerCount) {
        if (bathShowerCount == null) {
            return 0;
        }

        return switch (bathShowerCount) {
            case ONE -> 1;
            case TWO -> 2;
            case THREE -> 3;
            case FOUR_PLUS -> 4;
        };
    }

    private boolean matchesExactly(BoilerModel boiler, int requiredRadiators, int requiredBathShowerUnits) {
        return contains(requiredRadiators, boiler.getRadiatorsMin(), boiler.getRadiatorsMax())
                && contains(requiredBathShowerUnits, boiler.getBathroomsMin(), boiler.getBathroomsMax());
    }

    private boolean contains(int value, Integer min, Integer max) {
        return value >= safeNumber(min) && value <= safeNumber(max);
    }

    private Comparator<BoilerModel> exactMatchComparator(int requiredRadiators, int requiredBathShowerUnits) {
        return Comparator
                .comparingInt((BoilerModel boiler) -> fitScore(boiler, requiredRadiators, requiredBathShowerUnits))
                .thenComparingInt(boiler -> safeNumber(boiler.getAveragePriceGbp()))
                .thenComparingInt(boiler -> safeNumber(boiler.getPowerKw()))
                .thenComparing(boiler -> safeText(boiler.getBrand()))
                .thenComparing(boiler -> safeText(boiler.getModel()));
    }

    private Comparator<BoilerModel> closestMatchComparator(int requiredRadiators, int requiredBathShowerUnits) {
        return Comparator
                .comparingInt((BoilerModel boiler) -> mismatchScore(boiler, requiredRadiators, requiredBathShowerUnits))
                .thenComparingInt(boiler -> fitScore(boiler, requiredRadiators, requiredBathShowerUnits))
                .thenComparingInt(boiler -> safeNumber(boiler.getAveragePriceGbp()))
                .thenComparingInt(boiler -> safeNumber(boiler.getPowerKw()))
                .thenComparing(boiler -> safeText(boiler.getBrand()))
                .thenComparing(boiler -> safeText(boiler.getModel()));
    }

    private int fitScore(BoilerModel boiler, int requiredRadiators, int requiredBathShowerUnits) {
        return (safeNumber(boiler.getRadiatorsMax()) - requiredRadiators) * RADIATOR_WEIGHT
                + (safeNumber(boiler.getBathroomsMax()) - requiredBathShowerUnits) * BATH_SHOWER_WEIGHT;
    }

    private int mismatchScore(BoilerModel boiler, int requiredRadiators, int requiredBathShowerUnits) {
        return distanceFromRange(requiredRadiators, boiler.getRadiatorsMin(), boiler.getRadiatorsMax()) * RADIATOR_WEIGHT
                + distanceFromRange(requiredBathShowerUnits, boiler.getBathroomsMin(), boiler.getBathroomsMax()) * BATH_SHOWER_WEIGHT;
    }

    private int distanceFromRange(int value, Integer min, Integer max) {
        int safeMin = safeNumber(min);
        int safeMax = safeNumber(max);

        if (value < safeMin) {
            return safeMin - value;
        }

        if (value > safeMax) {
            return value - safeMax;
        }

        return 0;
    }

    private String toTargetTypeLabel(BoilerType boilerType) {
        if (boilerType == null) {
            return "Boiler";
        }

        return switch (boilerType) {
            case COMBI -> "Combi boiler";
            case SYSTEM -> "System boiler";
            case HEAT_ONLY -> "Heat-only boiler";
            case OTHER -> "Boiler";
        };
    }

    private int safeNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private String safeText(String value) {
        return Objects.toString(value, "");
    }
}
