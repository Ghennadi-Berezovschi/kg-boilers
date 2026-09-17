package com.kgboilers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgboilers.config.properties.CompanyProperties;
import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.dto.CallbackRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CallbackRequestServiceTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private JavaMailSender mailSender;
    private CallbackRequestService callbackRequestService;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        ObjectProvider<JavaMailSender> mailSenderProvider = mock(ObjectProvider.class);
        mailSender = mock(JavaMailSender.class);

        ContactProperties contactProperties = new ContactProperties();
        contactProperties.setEmail("office@kgboilers.co.uk");
        CompanyProperties companyProperties = new CompanyProperties();
        companyProperties.setName("Pro Boiler Services London");

        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(42L);

        callbackRequestService = new CallbackRequestService(
                jdbcTemplate,
                new ObjectMapper(),
                mailSenderProvider,
                contactProperties,
                companyProperties
        );
    }

    @Test
    void submit_shouldSaveLeadAndSendBusinessAndClientEmails() {
        CallbackRequestDto request = new CallbackRequestDto();
        request.setName("Jane Smith");
        request.setPhone("+44 7700 900123");
        request.setEmail("jane@example.com");
        request.setService("Boiler repair");
        request.setMessage("Please call me after 6pm");

        long id = callbackRequestService.submit(request);

        assertEquals(42L, id);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).queryForObject(any(String.class), paramsCaptor.capture(), eq(Long.class));
        MapSqlParameterSource params = paramsCaptor.getValue();
        assertEquals("CALLBACK", params.getValue("postcode"));
        assertEquals("CALLBACK_REQUEST", params.getValue("serviceType"));
        assertEquals("Request callback", params.getValue("selectedWork"));
        assertEquals("Jane Smith", params.getValue("clientName"));
        assertEquals("jane@example.com", params.getValue("clientEmail"));
        assertEquals("+44 7700 900123", params.getValue("clientPhone"));
        assertTrue(params.getValue("clientAnswersJson").toString().contains("Please call me after 6pm"));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());
        List<SimpleMailMessage> messages = messageCaptor.getAllValues();

        assertEquals("office@kgboilers.co.uk", messages.get(0).getTo()[0]);
        assertTrue(messages.get(0).getText().contains("New callback request received."));
        assertTrue(messages.get(0).getText().contains("Phone: +44 7700 900123"));
        assertTrue(messages.get(0).getText().contains("Service: Boiler repair"));

        assertEquals("jane@example.com", messages.get(1).getTo()[0]);
        assertTrue(messages.get(1).getText().contains("We received your callback request"));
    }

    @Test
    void submit_shouldSkipClientEmailWhenEmailIsBlank() {
        CallbackRequestDto request = new CallbackRequestDto();
        request.setName("Jane Smith");
        request.setPhone("+44 7700 900123");
        request.setEmail(" ");

        callbackRequestService.submit(request);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertEquals("office@kgboilers.co.uk", messageCaptor.getValue().getTo()[0]);
    }
}
