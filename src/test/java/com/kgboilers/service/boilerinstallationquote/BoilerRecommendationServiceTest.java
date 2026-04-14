package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.BoilerCatalogProperties;
import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.HeatOnlyConversion;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoilerRecommendationServiceTest {

    private BoilerRecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        BoilerCatalogProperties properties = new BoilerCatalogProperties();
        properties.setCombi(List.of(
                boiler("Vaillant", "ecoTEC Plus 25kW Combi", 25, 1, 9, 1, 1, 1750, true),
                boiler("Vaillant", "ecoTEC Plus 32kW Combi", 32, 8, 16, 2, 2, 2200, true)
        ));
        properties.setSystem(List.of(
                boiler("Main", "Eco Compact 15kW System Boiler", 15, 1, 8, 1, 1, 950, true)
        ));
        properties.setHeatOnly(List.of(
                boiler("Main", "Eco Compact 15kW", 15, 1, 8, 1, 1, 900, true),
                boiler("Vaillant", "ecoTEC Plus 30kW System", 30, 12, 18, 2, 3, 1900, true)
        ));

        BoilerCatalogService boilerCatalogService = new BoilerCatalogService(properties);
        recommendationService = new BoilerRecommendationService(boilerCatalogService);
    }

    @Test
    void recommend_shouldReturnExactHeatOnlyMatches_forMatchingDemand() {
        QuoteSessionState state = new QuoteSessionState();
        state.setBoilerType(BoilerType.HEAT_ONLY);
        state.setHeatOnlyConversion(HeatOnlyConversion.NO);
        state.setRadiatorCount(RadiatorCount.ZERO_TO_FIVE);
        state.setBathShowerCount(BathShowerCount.ONE);

        BoilerRecommendationResult result = recommendationService.recommend(state);

        assertEquals(BoilerType.HEAT_ONLY, result.getTargetType());
        assertEquals("Heat-only boiler", result.getTargetTypeLabel());
        assertEquals(5, result.getRequiredRadiators());
        assertEquals(1, result.getRequiredBathShowerUnits());
        assertTrue(result.isExactMatch());
        assertEquals(1, result.getBoilers().size());
        assertEquals("Eco Compact 15kW", result.getBoilers().get(0).getModel());
    }

    @Test
    void recommend_shouldUseCombiCatalog_whenHeatOnlyConversionIsYes() {
        QuoteSessionState state = new QuoteSessionState();
        state.setBoilerType(BoilerType.HEAT_ONLY);
        state.setHeatOnlyConversion(HeatOnlyConversion.YES);
        state.setRadiatorCount(RadiatorCount.ZERO_TO_FIVE);
        state.setBathShowerCount(BathShowerCount.ONE);

        BoilerRecommendationResult result = recommendationService.recommend(state);

        assertEquals(BoilerType.COMBI, result.getTargetType());
        assertTrue(result.isExactMatch());
        assertEquals("ecoTEC Plus 25kW Combi", result.getBoilers().get(0).getModel());
    }

    @Test
    void recommend_shouldReturnClosestMatches_whenExactMatchDoesNotExist() {
        QuoteSessionState state = new QuoteSessionState();
        state.setBoilerType(BoilerType.HEAT_ONLY);
        state.setHeatOnlyConversion(HeatOnlyConversion.NO);
        state.setRadiatorCount(RadiatorCount.SEVENTEEN_PLUS);
        state.setBathShowerCount(BathShowerCount.FOUR_PLUS);

        BoilerRecommendationResult result = recommendationService.recommend(state);

        assertFalse(result.isExactMatch());
        assertFalse(result.getBoilers().isEmpty());
        assertEquals("ecoTEC Plus 30kW System", result.getBoilers().get(0).getModel());
    }

    private BoilerModel boiler(String brand,
                               String model,
                               int powerKw,
                               int radiatorsMin,
                               int radiatorsMax,
                               int bathroomsMin,
                               int bathroomsMax,
                               int averagePriceGbp,
                               boolean enabled) {
        BoilerModel boiler = new BoilerModel();
        boiler.setBrand(brand);
        boiler.setModel(model);
        boiler.setPowerKw(powerKw);
        boiler.setRadiatorsMin(radiatorsMin);
        boiler.setRadiatorsMax(radiatorsMax);
        boiler.setBathroomsMin(bathroomsMin);
        boiler.setBathroomsMax(bathroomsMax);
        boiler.setAveragePriceGbp(averagePriceGbp);
        boiler.setEnabled(enabled);
        return boiler;
    }
}
