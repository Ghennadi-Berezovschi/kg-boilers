package com.kgboilers.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kgboilers.config.properties.CompanyProperties;
import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.dto.CallbackRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CallbackRequestService {

    private static final String CALLBACK_POSTCODE_PLACEHOLDER = "CALLBACK";
    private static final String CALLBACK_SERVICE_TYPE = "CALLBACK_REQUEST";
    private static final String CALLBACK_SELECTED_WORK = "Request callback";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ContactProperties contactProperties;
    private final CompanyProperties companyProperties;

    public CallbackRequestService(NamedParameterJdbcTemplate jdbcTemplate,
                                  ObjectMapper objectMapper,
                                  ObjectProvider<JavaMailSender> mailSenderProvider,
                                  ContactProperties contactProperties,
                                  CompanyProperties companyProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.mailSenderProvider = mailSenderProvider;
        this.contactProperties = contactProperties;
        this.companyProperties = companyProperties;
    }

    @Transactional
    public long submit(CallbackRequestDto request) {
        long id = saveCallbackLead(request);
        sendEmails(request);
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

    private void sendEmails(CallbackRequestDto request) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Callback request email was skipped because JavaMailSender is not configured");
            return;
        }

        if (hasText(contactProperties.getEmail())) {
            sendSafely(mailSender,
                    contactProperties.getEmail(),
                    "New callback request",
                    buildBusinessEmailBody(request));
        }

        if (hasText(request.getEmail())) {
            sendSafely(mailSender,
                    request.getEmail(),
                    "Your " + companyProperties.getName() + " callback request",
                    buildClientEmailBody(request));
        }
    }

    private String buildBusinessEmailBody(CallbackRequestDto request) {
        return """
                New callback request received.

                Customer details:
                Name: %s
                Phone: %s
                Email: %s

                Request details:
                Service: %s
                Message: %s
                """.formatted(
                clean(request.getName()),
                clean(request.getPhone()),
                displayOrDash(request.getEmail()),
                displayOrDash(request.getService()),
                displayOrDash(request.getMessage())
        );
    }

    private String buildClientEmailBody(CallbackRequestDto request) {
        return """
                Hello %s,

                Thank you for contacting %s.
                We received your callback request and will call you back soon.

                Your request:
                Phone: %s
                Service: %s

                Kind regards,
                %s
                """.formatted(
                clean(request.getName()),
                companyProperties.getName(),
                clean(request.getPhone()),
                displayOrDash(request.getService()),
                companyProperties.getName()
        );
    }

    private void sendSafely(JavaMailSender mailSender, String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Failed to send callback request email to {}", to, ex);
        }
    }

    private String displayOrDash(String value) {
        return hasText(value) ? value.trim() : "-";
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
