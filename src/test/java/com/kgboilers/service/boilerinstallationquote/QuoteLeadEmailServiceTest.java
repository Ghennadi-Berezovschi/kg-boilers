package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.QuoteOfferProperties;
import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        QuoteOfferProperties quoteOfferProperties = new QuoteOfferProperties();
        quoteOfferProperties.setIncludedItems(List.of(
                "Boiler installation",
                "Programmable Room Thermostat",
                "Disposal of your old boiler"
        ));

        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        quoteLeadEmailService = new QuoteLeadEmailService(mailSenderProvider, contactProperties, quoteOfferProperties);
    }

    @Test
    void sendLeadEmails_shouldSendClientAndBusinessEmails() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setFuel(FuelType.GAS);
        state.setOwnership(com.kgboilers.model.boilerinstallation.enums.OwnershipType.LANDLORD);
        state.setPropertyType(PropertyType.HOUSE);
        state.setRadiatorCount(RadiatorCount.SIX_TO_NINE);
        state.setBathShowerCount(BathShowerCount.TWO);
        state.setFlueType(FlueType.HORIZONTAL);
        state.setFlueLength(FlueLength.TWO_TO_THREE);

        quoteLeadEmailService.sendLeadEmails(
                state,
                "boiler-installation",
                "Vaillant ecoTEC Plus 28kW Combi",
                2050,
                "client@example.com",
                "+44 7700 900123",
                300,
                250,
                50,
                0,
                List.of(optionalExtra("hive-thermostat-mini", "Hive Thermostat Mini", 150)),
                150
        );

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        List<SimpleMailMessage> messages = messageCaptor.getAllValues();
        SimpleMailMessage clientMessage = messages.get(0);
        SimpleMailMessage businessMessage = messages.get(1);

        assertEquals("client@example.com", clientMessage.getTo()[0]);
        assertTrue(clientMessage.getText().contains("Your fixed price including installation:"));
        assertTrue(clientMessage.getText().contains("Boiler installation"));
        assertTrue(clientMessage.getText().contains("Programmable Room Thermostat"));
        assertTrue(clientMessage.getText().contains("Disposal of your old boiler"));
        assertTrue(clientMessage.getText().contains("Selected installation extras:"));
        assertTrue(clientMessage.getText().contains("Optional extras:"));
        assertTrue(clientMessage.getText().contains("Hive Thermostat Mini"));
        assertFalse(clientMessage.getText().contains("Client answers:"));
        assertFalse(clientMessage.getText().contains("Email: client@example.com"));

        assertEquals("office@kgboilers.co.uk", businessMessage.getTo()[0]);
        assertTrue(businessMessage.getText().contains("Client answers:"));
        assertTrue(businessMessage.getText().contains("Relocation price: £300"));
        assertTrue(businessMessage.getText().contains("Flue length price: £250"));
        assertTrue(businessMessage.getText().contains("Optional extras price: £150"));
        assertTrue(businessMessage.getText().contains("Status:"));
    }

    @Test
    void sendLeadEmails_shouldSkipWhenMailSenderIsUnavailable() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        quoteLeadEmailService.sendLeadEmails(
                new QuoteSessionState(),
                "boiler-installation",
                "Boiler",
                1000,
                "client@example.com",
                "+44 7700 900123",
                0,
                0,
                0,
                0,
                List.of(),
                0
        );

        verifyNoInteractions(mailSender);
    }

    private QuoteOptionalExtra optionalExtra(String id, String title, int priceGbp) {
        QuoteOptionalExtra extra = new QuoteOptionalExtra();
        extra.setId(id);
        extra.setTitle(title);
        extra.setPriceGbp(priceGbp);
        return extra;
    }
}
