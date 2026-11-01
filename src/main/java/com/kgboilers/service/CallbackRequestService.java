package com.kgboilers.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kgboilers.dto.CallbackRequestDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CallbackRequestService {

    private static final String CALLBACK_POSTCODE_PLACEHOLDER = "CALLBACK";
    private static final String CALLBACK_SERVICE_TYPE = "CALLBACK_REQUEST";
    private static final String CALLBACK_SELECTED_WORK = "Request callback";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher events;

    public CallbackRequestService(NamedParameterJdbcTemplate jdbcTemplate,
                                  ObjectMapper objectMapper, ApplicationEventPublisher events) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.events = events;
    }

    @Transactional
    public long submit(CallbackRequestDto request) {
        long id = saveCallbackLead(request);
        events.publishEvent(new CallbackRequestedEvent(clean(request.getName()), clean(request.getPhone()),
                clean(request.getEmail()), clean(request.getService()), clean(request.getMessage())));
        return id;
    }

    private long saveCallbackLead(CallbackRequestDto request) {
        Long id = jdbcTemplate.queryForObject("""
                INSERT INTO quotes (
                    postcode,
                    service_type,
                    selected_boiler,
                    client_name,
                    client_email,
                    client_phone,
                    status,
                    contact_requested_at,
                    client_answers_json,
                    selected_optional_extras_json,
                    optional_extras_price_gbp,
                    created_at,
                    updated_at
                )
                VALUES (
                    :postcode,
                    :serviceType,
                    :selectedWork,
                    :clientName,
                    :clientEmail,
                    :clientPhone,
                    'NEW_LEAD',
                    CURRENT_TIMESTAMP,
                    CAST(:clientAnswersJson AS jsonb),
                    '[]'::jsonb,
                    0,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP
                )
                RETURNING id
                """, new MapSqlParameterSource()
                .addValue("postcode", CALLBACK_POSTCODE_PLACEHOLDER)
                .addValue("serviceType", CALLBACK_SERVICE_TYPE)
                .addValue("selectedWork", CALLBACK_SELECTED_WORK)
                .addValue("clientName", clean(request.getName()))
                .addValue("clientEmail", cleanToNull(request.getEmail()))
                .addValue("clientPhone", clean(request.getPhone()))
                .addValue("clientAnswersJson", clientAnswersJson(request)), Long.class);

        if (id == null) {
            throw new IllegalStateException("Callback request was saved without returning an id.");
        }
        return id;
    }

    private String clientAnswersJson(CallbackRequestDto request) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode answers = root.putObject("answers");
        answers.put("requestType", "Request callback");
        answers.put("name", clean(request.getName()));
        answers.put("phone", clean(request.getPhone()));
        answers.put("email", clean(request.getEmail()));
        answers.put("service", clean(request.getService()));
        answers.put("message", clean(request.getMessage()));
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not save callback request details.", ex);
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
