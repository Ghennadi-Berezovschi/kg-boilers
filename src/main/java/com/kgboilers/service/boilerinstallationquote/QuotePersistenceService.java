package com.kgboilers.service.boilerinstallationquote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class QuotePersistenceService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public QuotePersistenceService(NamedParameterJdbcTemplate jdbcTemplate,
                                   ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Long saveOrUpdate(Long savedQuoteId,
                             String serviceType,
                             QuoteSessionState state,
                             BoilerRecommendationResult recommendation,
                             int relocationPriceGbp,
                             int flueLengthPriceGbp,
                             int fluePositionPriceGbp,
                             int flueClearancePriceGbp,
                             List<QuoteOptionalExtra> selectedOptionalExtras,
                             int optionalExtrasPriceGbp) {

        MapSqlParameterSource params = buildParameters(
                serviceType,
                state,
                recommendation,
                relocationPriceGbp,
                flueLengthPriceGbp,
                fluePositionPriceGbp,
                flueClearancePriceGbp,
                selectedOptionalExtras,
                optionalExtrasPriceGbp
        );

        if (savedQuoteId == null) {
            return jdbcTemplate.queryForObject("""
                    INSERT INTO quotes (
                        postcode,
                        fuel,
                        ownership,
                        property_type,
                        service_type,
                        bedrooms,
                        boiler_type,
                        boiler_position,
                        boiler_location,
                        boiler_condition,
                        relocation,
                        relocation_distance,
                        flue_type,
                        vertical_flue_type,
                        flue_length,
                        flue_position,
                        flue_clearance,
                        flue_property_distance,
                        radiator_count,
                        bath_shower_count,
                        recommended_boiler,
                        installation_price_gbp,
                        selected_optional_extras_json,
                        optional_extras_price_gbp,
                        client_answers_json,
                        created_at,
                        updated_at
                    ) VALUES (
                        :postcode,
                        :fuel,
                        :ownership,
                        :propertyType,
                        :serviceType,
                        :bedrooms,
                        :boilerType,
                        :boilerPosition,
                        :boilerLocation,
                        :boilerCondition,
                        :relocation,
                        :relocationDistance,
                        :flueType,
                        :verticalFlueType,
                        :flueLength,
                        :fluePosition,
                        :flueClearance,
                        :fluePropertyDistance,
                        :radiatorCount,
                        :bathShowerCount,
                        :recommendedBoiler,
                        :installationPriceGbp,
                        CAST(:selectedOptionalExtrasJson AS jsonb),
                        :optionalExtrasPriceGbp,
                        CAST(:clientAnswersJson AS jsonb),
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    RETURNING id
                    """, params, Long.class);
        }

        params.addValue("id", savedQuoteId);
        jdbcTemplate.update("""
                UPDATE quotes
                SET postcode = :postcode,
                    fuel = :fuel,
                    ownership = :ownership,
                    property_type = :propertyType,
                    service_type = :serviceType,
                    bedrooms = :bedrooms,
                    boiler_type = :boilerType,
                    boiler_position = :boilerPosition,
                    boiler_location = :boilerLocation,
                    boiler_condition = :boilerCondition,
                    relocation = :relocation,
                    relocation_distance = :relocationDistance,
                    flue_type = :flueType,
                    vertical_flue_type = :verticalFlueType,
                    flue_length = :flueLength,
                    flue_position = :fluePosition,
                    flue_clearance = :flueClearance,
                    flue_property_distance = :fluePropertyDistance,
                    radiator_count = :radiatorCount,
                    bath_shower_count = :bathShowerCount,
                    recommended_boiler = :recommendedBoiler,
                    installation_price_gbp = :installationPriceGbp,
                    selected_optional_extras_json = CAST(:selectedOptionalExtrasJson AS jsonb),
                    optional_extras_price_gbp = :optionalExtrasPriceGbp,
                    client_answers_json = CAST(:clientAnswersJson AS jsonb),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """, params);
        return savedQuoteId;
    }

    @Transactional
    public Long saveLead(Long savedQuoteId,
                         String serviceType,
                         QuoteSessionState state,
                         BoilerRecommendationResult recommendation,
                         int relocationPriceGbp,
                         int flueLengthPriceGbp,
                         int fluePositionPriceGbp,
                         int flueClearancePriceGbp,
                         List<QuoteOptionalExtra> selectedOptionalExtras,
                         int optionalExtrasPriceGbp,
                         String selectedBoiler,
                         String clientName,
                         String email,
                         String phone) {
        Long quoteId = saveOrUpdate(
                savedQuoteId,
                serviceType,
                state,
                recommendation,
                relocationPriceGbp,
                flueLengthPriceGbp,
                fluePositionPriceGbp,
                flueClearancePriceGbp,
                selectedOptionalExtras,
                optionalExtrasPriceGbp
        );

        saveContactDetails(quoteId, selectedBoiler, clientName, email, phone);
        return quoteId;
    }

    @Transactional
    public Long saveRepairLead(Long savedQuoteId,
                               String serviceType,
                               QuoteSessionState state,
                               String clientName,
                               String email,
                               String phone) {
        Long quoteId = saveOrUpdate(
                savedQuoteId,
                serviceType,
                state,
                null,
                0,
                0,
                0,
                0,
                Collections.emptyList(),
                0
        );

        saveContactDetails(quoteId, "Boiler Repair Request", clientName, email, phone);
        return quoteId;
    }

    public void saveContactDetails(Long quoteId,
                                   String selectedBoiler,
                                   String clientName,
                                   String email,
                                   String phone) {
        if (quoteId == null) {
            throw new IllegalArgumentException("Quote ID is required to save contact details");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", quoteId)
                .addValue("selectedBoiler", selectedBoiler)
                .addValue("clientName", clientName)
                .addValue("clientEmail", email)
                .addValue("clientPhone", phone);

        jdbcTemplate.update("""
                UPDATE quotes
                SET selected_boiler = :selectedBoiler,
                    client_name = :clientName,
                    client_email = :clientEmail,
                    client_phone = :clientPhone,
                    status = 'NEW_LEAD',
                    contact_requested_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """, params);
    }

    private MapSqlParameterSource buildParameters(String serviceType,
                                                  QuoteSessionState state,
                                                  BoilerRecommendationResult recommendation,
                                                  int relocationPriceGbp,
                                                  int flueLengthPriceGbp,
                                                  int fluePositionPriceGbp,
                                                  int flueClearancePriceGbp,
                                                  List<QuoteOptionalExtra> selectedOptionalExtras,
                                                  int optionalExtrasPriceGbp) {

        int extrasTotalPriceGbp = relocationPriceGbp + flueLengthPriceGbp + fluePositionPriceGbp + flueClearancePriceGbp;
        BoilerModel primaryBoiler = getPrimaryBoiler(recommendation);
        Integer installationPriceGbp = primaryBoiler != null
                ? primaryBoiler.getAveragePriceGbp() + extrasTotalPriceGbp + optionalExtrasPriceGbp
                : null;

        return new MapSqlParameterSource()
                .addValue("postcode", state.getPostcode())
                .addValue("fuel", enumName(state.getFuel()))
                .addValue("ownership", enumName(state.getOwnership()))
                .addValue("propertyType", enumName(state.getPropertyType()))
                .addValue("serviceType", serviceType)
                .addValue("bedrooms", enumName(state.getBedrooms()))
                .addValue("boilerType", enumName(state.getBoilerType()))
                .addValue("boilerPosition", enumName(state.getBoilerPosition()))
                .addValue("boilerLocation", enumName(state.getBoilerLocation()))
                .addValue("boilerFloorLevel", enumName(state.getBoilerFloorLevel()))
                .addValue("boilerCondition", enumName(state.getBoilerCondition()))
                .addValue("relocation", enumName(state.getRelocation()))
                .addValue("relocationDistance", enumName(state.getRelocationDistance()))
                .addValue("flueType", enumName(state.getFlueType()))
                .addValue("verticalFlueType", enumName(state.getVerticalFlueType()))
                .addValue("flueLength", enumName(state.getFlueLength()))
                .addValue("fluePosition", enumName(state.getFluePosition()))
                .addValue("flueClearance", enumName(state.getFlueClearance()))
                .addValue("fluePropertyDistance", enumName(state.getFluePropertyDistance()))
                .addValue("radiatorCount", enumName(state.getRadiatorCount()))
                .addValue("bathShowerCount", enumName(state.getBathShowerCount()))
                .addValue("recommendedBoiler", buildRecommendedBoilerLabel(primaryBoiler))
                .addValue("installationPriceGbp", installationPriceGbp)
                .addValue("selectedOptionalExtrasJson", toJson(buildSelectedOptionalExtrasSnapshot(selectedOptionalExtras)))
                .addValue("optionalExtrasPriceGbp", optionalExtrasPriceGbp)
                .addValue("clientAnswersJson", toJson(buildClientAnswersSnapshot(
                        serviceType,
                        state,
                        recommendation,
                        relocationPriceGbp,
                        flueLengthPriceGbp,
                        fluePositionPriceGbp,
                        flueClearancePriceGbp,
                        selectedOptionalExtras,
                        optionalExtrasPriceGbp,
                        installationPriceGbp
                )));
    }

    private Map<String, Object> buildClientAnswersSnapshot(String serviceType,
                                                           QuoteSessionState state,
                                                           BoilerRecommendationResult recommendation,
                                                           int relocationPriceGbp,
                                                           int flueLengthPriceGbp,
                                                           int fluePositionPriceGbp,
                                                           int flueClearancePriceGbp,
                                                           List<QuoteOptionalExtra> selectedOptionalExtras,
                                                           int optionalExtrasPriceGbp,
                                                           Integer installationPriceGbp) {

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("serviceType", serviceType);

        Map<String, Object> answers = new LinkedHashMap<>();
        answers.put("postcode", state.getPostcode());
        answers.put("fuel", enumName(state.getFuel()));
        answers.put("ownership", enumName(state.getOwnership()));
        answers.put("propertyType", enumName(state.getPropertyType()));
        answers.put("bedrooms", enumName(state.getBedrooms()));
        answers.put("boilerType", enumName(state.getBoilerType()));
        answers.put("boilerMake", enumName(state.getBoilerMake()));
        answers.put("boilerAge", state.getBoilerAgeSummary());
        answers.put("boilerPosition", enumName(state.getBoilerPosition()));
        answers.put("boilerLocation", enumName(state.getBoilerLocation()));
        answers.put("powerFlushStatus", state.getPowerFlushSummary());
        answers.put("magneticFilterStatus", state.getMagneticFilterSummary());
        answers.put("repairProblem", state.getRepairProblemSummary());
        answers.put("boilerPressureStatus", state.getBoilerPressureSummary());
        answers.put("faultCodeDisplayStatus", state.getFaultCodeDisplaySummary());
        answers.put("faultCodeDetails", state.getFaultCodeDetailsSummary());
        answers.put("boilerFloorLevel", enumName(state.getBoilerFloorLevel()));
        answers.put("boilerCondition", enumName(state.getBoilerCondition()));
        answers.put("relocation", enumName(state.getRelocation()));
        answers.put("relocationDistance", state.getRelocationDistanceSummary());
        answers.put("flueType", state.getFlueSummary());
        answers.put("verticalFlueType", enumName(state.getVerticalFlueType()));
        answers.put("flueLength", state.getFlueLengthSummary());
        answers.put("slopedRoofPosition", state.getSlopedRoofPositionSummary());
        answers.put("fluePosition", state.getFluePositionSummary());
        answers.put("flueClearance", state.getFlueClearanceSummary());
        answers.put("fluePropertyDistance", state.getFluePropertyDistanceSummary());
        answers.put("radiatorCount", state.getRadiatorCountSummary());
        answers.put("bathShowerCount", state.getBathShowerCountSummary());
        snapshot.put("answers", answers);

        Map<String, Object> pricing = new LinkedHashMap<>();
        pricing.put("relocationPriceGbp", relocationPriceGbp);
        pricing.put("flueLengthPriceGbp", flueLengthPriceGbp);
        pricing.put("fluePositionPriceGbp", fluePositionPriceGbp);
        pricing.put("flueClearancePriceGbp", flueClearancePriceGbp);
        pricing.put("optionalExtrasPriceGbp", optionalExtrasPriceGbp);
        pricing.put("installationPriceGbp", installationPriceGbp);
        snapshot.put("pricing", pricing);

        snapshot.put("selectedOptionalExtras", buildSelectedOptionalExtrasSnapshot(selectedOptionalExtras));

        Map<String, Object> recommendationData = new LinkedHashMap<>();
        recommendationData.put("targetType", recommendation != null ? enumName(recommendation.getTargetType()) : null);
        recommendationData.put("exactMatch", recommendation != null && recommendation.isExactMatch());
        recommendationData.put("boilers", buildRecommendedBoilersSnapshot(recommendation));
        snapshot.put("recommendation", recommendationData);

        return snapshot;
    }

    private List<Map<String, Object>> buildRecommendedBoilersSnapshot(BoilerRecommendationResult recommendation) {
        if (recommendation == null || recommendation.getBoilers() == null) {
            return List.of();
        }

        return recommendation.getBoilers().stream()
                .map(boiler -> {
                    Map<String, Object> boilerData = new LinkedHashMap<>();
                    boilerData.put("brand", boiler.getBrand());
                    boilerData.put("model", boiler.getModel());
                    boilerData.put("powerKw", boiler.getPowerKw());
                    boilerData.put("averagePriceGbp", boiler.getAveragePriceGbp());
                    boilerData.put("image", boiler.getImage());
                    return boilerData;
                })
                .toList();
    }

    private List<Map<String, Object>> buildSelectedOptionalExtrasSnapshot(List<QuoteOptionalExtra> selectedOptionalExtras) {
        if (selectedOptionalExtras == null || selectedOptionalExtras.isEmpty()) {
            return List.of();
        }

        return selectedOptionalExtras.stream()
                .map(extra -> {
                    Map<String, Object> extraData = new LinkedHashMap<>();
                    extraData.put("id", extra.getId());
                    extraData.put("title", extra.getTitle());
                    extraData.put("description", extra.getDescription());
                    extraData.put("priceGbp", extra.getPriceGbp());
                    extraData.put("image", extra.getImage());
                    return extraData;
                })
                .toList();
    }

    private BoilerModel getPrimaryBoiler(BoilerRecommendationResult recommendation) {
        if (recommendation == null || recommendation.getBoilers() == null || recommendation.getBoilers().isEmpty()) {
            return null;
        }
        return recommendation.getBoilers().getFirst();
    }

    private String buildRecommendedBoilerLabel(BoilerModel boiler) {
        if (boiler == null) {
            return null;
        }
        return boiler.getBrand() + " " + boiler.getModel();
    }

    private String enumName(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    private String toJson(Object snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize quote answers", ex);
        }
    }
}
