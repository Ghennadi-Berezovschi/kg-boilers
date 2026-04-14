package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuoteLeadEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ContactProperties contactProperties;

    public QuoteLeadEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                 ContactProperties contactProperties) {
        this.mailSenderProvider = mailSenderProvider;
        this.contactProperties = contactProperties;
    }

    public void sendLeadEmails(QuoteSessionState state,
                               String selectedBoiler,
                               int totalPriceGbp,
                               String clientEmail,
                               String clientPhone) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Quote lead email was skipped because JavaMailSender is not configured");
            return;
        }

        sendSafely(mailSender,
                clientEmail,
                "Your K&G Boilers quote request",
                buildClientEmailBody(state, selectedBoiler, totalPriceGbp, clientEmail, clientPhone));

        if (contactProperties.getEmail() != null && !contactProperties.getEmail().isBlank()) {
            sendSafely(mailSender,
                    contactProperties.getEmail(),
                    "New boiler lead: " + selectedBoiler,
                    buildBusinessEmailBody(state, selectedBoiler, totalPriceGbp, clientEmail, clientPhone));
        }
    }

    private void sendSafely(JavaMailSender mailSender,
                            String to,
                            String subject,
                            String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send quote lead email", ex);
        }
    }

    private String buildClientEmailBody(QuoteSessionState state,
                                        String selectedBoiler,
                                        int totalPriceGbp,
                                        String clientEmail,
                                        String clientPhone) {
        return """
                Thank you for your boiler request with K&G Boilers.

                Your selected boiler:
                %s

                Boiler price installation:
                £%d

                Your contact details:
                Email: %s
                Phone: %s

                Your property details:
                Postcode: %s
                Fuel: %s
                Property: %s
                Radiators: %s
                Baths and showers: %s
                Flue type: %s
                Flue length: %s

                We will contact you shortly.
                """.formatted(
                selectedBoiler,
                totalPriceGbp,
                clientEmail,
                clientPhone,
                stateSafe(state.getPostcode()),
                stateSafe(state.getFuel()),
                stateSafe(state.getPropertyType()),
                state.getRadiatorCountSummary(),
                state.getBathShowerCountSummary(),
                state.getFlueSummary(),
                state.getFlueLengthSummary()
        );
    }

    private String buildBusinessEmailBody(QuoteSessionState state,
                                          String selectedBoiler,
                                          int totalPriceGbp,
                                          String clientEmail,
                                          String clientPhone) {
        return """
                New boiler lead received.

                Selected boiler:
                %s

                Boiler price installation:
                £%d

                Client contact:
                Email: %s
                Phone: %s

                Client answers:
                Postcode: %s
                Fuel: %s
                Ownership: %s
                Property: %s
                Bedrooms: %s
                Boiler type: %s
                Boiler position: %s
                Boiler location: %s
                Boiler condition: %s
                Relocation: %s
                Relocation distance: %s
                Flue type: %s
                Flue length: %s
                Flue position: %s
                Flue clearance: %s
                Flue property distance: %s
                Radiators: %s
                Baths and showers: %s
                """.formatted(
                selectedBoiler,
                totalPriceGbp,
                clientEmail,
                clientPhone,
                stateSafe(state.getPostcode()),
                stateSafe(state.getFuel()),
                stateSafe(state.getOwnership()),
                stateSafe(state.getPropertyType()),
                stateSafe(state.getBedrooms()),
                stateSafe(state.getBoilerType()),
                stateSafe(state.getBoilerPosition()),
                stateSafe(state.getBoilerLocation()),
                stateSafe(state.getBoilerCondition()),
                stateSafe(state.getRelocation()),
                state.getRelocationDistanceSummary(),
                state.getFlueSummary(),
                state.getFlueLengthSummary(),
                state.getFluePositionSummary(),
                state.getFlueClearanceSummary(),
                state.getFluePropertyDistanceSummary(),
                state.getRadiatorCountSummary(),
                state.getBathShowerCountSummary()
        );
    }

    private String stateSafe(Object value) {
        return value == null ? "" : value.toString();
    }
}
