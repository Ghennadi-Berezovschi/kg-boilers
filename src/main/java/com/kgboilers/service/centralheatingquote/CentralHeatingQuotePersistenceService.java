package com.kgboilers.service.centralheatingquote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgboilers.model.centralheatingquote.CentralHeatingInstallationItem;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CentralHeatingQuotePersistenceService {

    private static final String LEAD_LABEL = "Central Heating Installation & Repair";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public CentralHeatingQuotePersistenceService(NamedParameterJdbcTemplate jdbcTemplate,
                                                 ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Long saveLead(Long savedQuoteId,
                         String serviceType,
                         CentralHeatingQuoteSessionState state,
                         String email,
                         String phone) {
        Long quoteId = saveOrUpdate(savedQuoteId, serviceType, state);
        saveContactDetails(quoteId, email, phone);
        return quoteId;
    }

    public Long saveOrUpdate(Long savedQuoteId,
                             String serviceType,
                             CentralHeatingQuoteSessionState state) {
        MapSqlParameterSource params = buildParameters(serviceType, state);

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
                        radiator_count,
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
                        :radiatorCount,
                        :leadLabel,
                        NULL,
                        CAST(:selectedOptionalExtrasJson AS jsonb),
                        0,
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
                    radiator_count = :radiatorCount,
                    recommended_boiler = :leadLabel,
                    installation_price_gbp = NULL,
                    selected_optional_extras_json = CAST(:selectedOptionalExtrasJson AS jsonb),
                    optional_extras_price_gbp = 0,
                    client_answers_json = CAST(:clientAnswersJson AS jsonb),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """, params);
        return savedQuoteId;
    }

    public void saveContactDetails(Long quoteId,
                                   String email,
                                   String phone) {
        if (quoteId == null) {
            throw new IllegalArgumentException("Quote ID is required to save contact details");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", quoteId)
                .addValue("leadLabel", LEAD_LABEL)
                .addValue("clientEmail", email)
                .addValue("clientPhone", phone);

        jdbcTemplate.update("""
                UPDATE quotes
                SET selected_boiler = :leadLabel,
                    client_email = :clientEmail,
                    client_phone = :clientPhone,
                    status = 'NEW_LEAD',
                    contact_requested_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """, params);
    }

    private MapSqlParameterSource buildParameters(String serviceType,
                                                  CentralHeatingQuoteSessionState state) {
        return new MapSqlParameterSource()
                .addValue("postcode", state.getPostcode())
                .addValue("fuel", enumName(state.getFuel()))
                .addValue("ownership", enumName(state.getOwnership()))
                .addValue("propertyType", enumName(state.getPropertyType()))
                .addValue("serviceType", serviceType)
                .addValue("bedrooms", enumName(state.getBedrooms()))
                .addValue("boilerType", enumName(state.getBoilerType()))
                .addValue("radiatorCount", enumName(state.getRadiatorCount()))
                .addValue("leadLabel", LEAD_LABEL)
                .addValue("selectedOptionalExtrasJson", "[]")
                .addValue("clientAnswersJson", toJson(buildClientAnswersSnapshot(serviceType, state)));
    }

    private Map<String, Object> buildClientAnswersSnapshot(String serviceType,
                                                           CentralHeatingQuoteSessionState state) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("serviceType", serviceType);
        snapshot.put("postcode", state.getPostcode());
        snapshot.put("ownership", label(state.getOwnership()));
        snapshot.put("propertyType", label(state.getPropertyType()));
        snapshot.put("bedrooms", label(state.getBedrooms()));
        snapshot.put("boilerType", label(state.getBoilerType()));
        snapshot.put("fuel", label(state.getFuel()));
        snapshot.put("radiatorCount", state.getRadiatorCountSummary());
        snapshot.put("trvValveStatus", state.getTrvValveStatusSummary());
        snapshot.put("powerFlushStatus", state.getPowerFlushStatus() != null ? state.getPowerFlushStatus().getLabel() : null);
        snapshot.put("magneticFilterStatus", state.getMagneticFilterStatus() != null ? state.getMagneticFilterStatus().getLabel() : null);
        snapshot.put("radiatorIssues", state.getRadiatorIssues().stream().map(issue -> issue.getLabel()).toList());
        snapshot.put("otherRadiatorIssueDetails", state.getOtherRadiatorIssueDetails());
        snapshot.put("valveQuantities", buildValveQuantitiesSnapshot(state));
        snapshot.put("installationItems", buildInstallationItemsSnapshot(state.getInstallationItems()));
        return snapshot;
    }

    private Map<String, Object> buildValveQuantitiesSnapshot(CentralHeatingQuoteSessionState state) {
        Map<String, Object> valveQuantities = new LinkedHashMap<>();
        valveQuantities.put("trvValves", state.getTrvValvesQuantity());
        valveQuantities.put("lockshieldValves", state.getLockshieldValvesQuantity());
        valveQuantities.put("towelRailValves", state.getTowelRailValvesQuantity());
        return valveQuantities;
    }

    private List<Map<String, Object>> buildInstallationItemsSnapshot(List<CentralHeatingInstallationItem> installationItems) {
        return installationItems.stream()
                .map(item -> {
                    Map<String, Object> itemSnapshot = new LinkedHashMap<>();
                    itemSnapshot.put("type", item.getInstallationItemType() != null ? item.getInstallationItemType().getLabel() : null);
                    itemSnapshot.put("position", item.getInstallationPositionType() != null ? item.getInstallationPositionType().getLabel() : null);
                    itemSnapshot.put("moveDistance", item.getMoveDistance() != null ? item.getMoveDistance().getValue() : null);
                    itemSnapshot.put("nearestPipeDistance", item.getNearestPipeDistance() != null ? item.getNearestPipeDistance().getValue() : null);
                    itemSnapshot.put("radiatorConvectorType", item.getRadiatorConvectorType() != null ? item.getRadiatorConvectorType().getLabel() : null);
                    itemSnapshot.put("lengthMm", item.getLengthMm());
                    itemSnapshot.put("widthMm", item.getWidthMm());
                    itemSnapshot.put("quantity", item.getQuantity());
                    itemSnapshot.put("summary", item.getSummary());
                    return itemSnapshot;
                })
                .toList();
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private String label(Enum<?> value) {
        return value == null ? null : value.toString();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize central heating quote snapshot", ex);
        }
    }
}
