package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuoteLeadEmailServiceTest {

    private ObjectProvider<JavaMailSender> mailSenderProvider;
    private JavaMailSender mailSender;
    private QuoteLeadEmailService quoteLeadEmailService;

    @BeforeEach
    void setUp() {
        mailSenderProvider = mock(ObjectProvider.class);
        mailSender = mock(JavaMailSender.class);

        ContactProperties contactProperties = new ContactProperties();
        contactProperties.setEmail("office@kgboilers.co.uk");

        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        quoteLeadEmailService = new QuoteLeadEmailService(mailSenderProvider, contactProperties);
    }

    @Test
    void sendLeadEmails_shouldSendClientAndBusinessEmails() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setFuel(FuelType.GAS);
        state.setPropertyType(PropertyType.HOUSE);
        state.setRadiatorCount(RadiatorCount.SIX_TO_NINE);
        state.setBathShowerCount(BathShowerCount.TWO);
        state.setFlueType(FlueType.HORIZONTAL);
        state.setFlueLength(FlueLength.TWO_TO_THREE);

        quoteLeadEmailService.sendLeadEmails(
                state,
                "Vaillant ecoTEC Plus 28kW Combi",
                2050,
                "client@example.com",
                "+44 7700 900123"
        );

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendLeadEmails_shouldSkipWhenMailSenderIsUnavailable() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        quoteLeadEmailService.sendLeadEmails(
                new QuoteSessionState(),
                "Boiler",
                1000,
                "client@example.com",
                "+44 7700 900123"
        );

        verifyNoInteractions(mailSender);
    }
}
