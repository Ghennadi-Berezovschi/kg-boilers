package com.kgboilers.service.boilerinstallationquote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BoilerCondition;
import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import com.kgboilers.model.boilerinstallation.enums.BoilerPosition;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueClearance;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import com.kgboilers.model.boilerinstallation.enums.FluePosition;
import com.kgboilers.model.boilerinstallation.enums.FluePropertyDistance;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuotePersistenceServiceTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private QuotePersistenceService quotePersistenceService;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        quotePersistenceService = new QuotePersistenceService(jdbcTemplate, new ObjectMapper());
    }

    @Test
    void saveOrUpdate_shouldInsertQuoteWithSnapshotAndInstallationPrice() {
        QuoteSessionState state = completeState();
        BoilerModel boiler = new BoilerModel();
        boiler.setBrand("Vaillant");
        boiler.setModel("ecoTEC Plus 28kW Combi");
        boiler.setPowerKw(28);
        boiler.setAveragePriceGbp(2000);
        boiler.setImage("/images/boilers/catalog/vaillant-ecotec-plus-28kw-combi.jpg");

        BoilerRecommendationResult recommendation = new BoilerRecommendationResult(
                BoilerType.COMBI,
                "Combi boiler",
                9,
                2,
                true,
                List.of(boiler)
        );

        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(77L);

        Long quoteId = quotePersistenceService.saveOrUpdate(
                null,
                "boiler-installation",
                state,
                recommendation,
                300,
                250,
                50,
                150
        );

        assertEquals(77L, quoteId);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), paramsCaptor.capture(), eq(Long.class));

        assertTrue(sqlCaptor.getValue().contains("INSERT INTO quotes"));

        MapSqlParameterSource params = paramsCaptor.getValue();
        assertEquals("E16 4JJ", params.getValue("postcode"));
        assertEquals("GAS", params.getValue("fuel"));
        assertEquals("HOMEOWNER", params.getValue("ownership"));
        assertEquals("boiler-installation", params.getValue("serviceType"));
        assertEquals("COMBI", params.getValue("boilerType"));
        assertEquals("UNDER_STRUCTURE", params.getValue("fluePosition"));
        assertEquals("Vaillant ecoTEC Plus 28kW Combi", params.getValue("recommendedBoiler"));
        assertEquals(2750, params.getValue("installationPriceGbp"));

        String snapshotJson = (String) params.getValue("clientAnswersJson");
        assertTrue(snapshotJson.contains("\"serviceType\":\"boiler-installation\""));
        assertTrue(snapshotJson.contains("\"postcode\":\"E16 4JJ\""));
        assertTrue(snapshotJson.contains("\"flueLengthPriceGbp\":250"));
        assertTrue(snapshotJson.contains("\"installationPriceGbp\":2750"));
        assertTrue(snapshotJson.contains("\"model\":\"ecoTEC Plus 28kW Combi\""));
    }

    @Test
    void saveOrUpdate_shouldUpdateExistingQuoteAndKeepNullRecommendationFieldsWhenAbsent() {
        QuoteSessionState state = completeState();

        Long quoteId = quotePersistenceService.saveOrUpdate(
                99L,
                "boiler-installation",
                state,
                null,
                0,
                250,
                0,
                150
        );

        assertEquals(99L, quoteId);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), paramsCaptor.capture());

        assertTrue(sqlCaptor.getValue().contains("UPDATE quotes"));

        MapSqlParameterSource params = paramsCaptor.getValue();
        assertEquals(99L, params.getValue("id"));
        assertNull(params.getValue("recommendedBoiler"));
        assertNull(params.getValue("installationPriceGbp"));

        String snapshotJson = (String) params.getValue("clientAnswersJson");
        assertTrue(snapshotJson.contains("\"exactMatch\":false"));
        assertTrue(snapshotJson.contains("\"boilers\":[]"));
    }

    @Test
    void saveContactDetails_shouldUpdateSelectedBoilerAndClientContacts() {
        quotePersistenceService.saveContactDetails(
                44L,
                "Main Eco Compact 30kW Combi",
                "client@example.com",
                "+44 7700 900123"
        );

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), paramsCaptor.capture());

        assertTrue(sqlCaptor.getValue().contains("selected_boiler"));

        MapSqlParameterSource params = paramsCaptor.getValue();
        assertEquals(44L, params.getValue("id"));
        assertEquals("Main Eco Compact 30kW Combi", params.getValue("selectedBoiler"));
        assertEquals("client@example.com", params.getValue("clientEmail"));
        assertEquals("+44 7700 900123", params.getValue("clientPhone"));
    }

    @Test
    void saveContactDetails_shouldRejectMissingQuoteId() {
        assertThrows(IllegalArgumentException.class,
                () -> quotePersistenceService.saveContactDetails(null, "Boiler", "client@example.com", "+44 7700 900123"));
    }

    private QuoteSessionState completeState() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setFuel(FuelType.GAS);
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);
        state.setBedrooms(Bedrooms.TWO);
        state.setBoilerType(BoilerType.COMBI);
        state.setBoilerPosition(BoilerPosition.WALL_MOUNTED);
        state.setBoilerLocation(BoilerLocation.BASEMENT);
        state.setBoilerCondition(BoilerCondition.NOT_WORKING);
        state.setRelocation(Relocation.YES);
        state.setRelocationDistance(RelocationDistance.TWO_TO_THREE);
        state.setFlueType(FlueType.HORIZONTAL);
        state.setFlueLength(FlueLength.TWO_TO_THREE);
        state.setFluePosition(FluePosition.UNDER_STRUCTURE);
        state.setFlueClearance(FlueClearance.LESS_THAN_THIRTY_CM);
        state.setFluePropertyDistance(FluePropertyDistance.MORE_THAN_ONE_METRE);
        state.setRadiatorCount(RadiatorCount.SIX_TO_NINE);
        state.setBathShowerCount(BathShowerCount.TWO);
        return state;
    }
}
