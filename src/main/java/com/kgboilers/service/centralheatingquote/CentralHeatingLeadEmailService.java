package com.kgboilers.service.centralheatingquote;

import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.config.properties.CompanyProperties;
import com.kgboilers.model.centralheatingquote.CentralHeatingInstallationItem;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CentralHeatingLeadEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ContactProperties contactProperties;
    private final CompanyProperties companyProperties;

    public CentralHeatingLeadEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                          ContactProperties contactProperties,
                                          CompanyProperties companyProperties) {
        this.mailSenderProvider = mailSenderProvider;
        this.contactProperties = contactProperties;
        this.companyProperties = companyProperties;
    }

    public void sendLeadEmails(CentralHeatingQuoteSessionState state,
                               String serviceType,
                               String clientName,
                               String clientEmail,
                               String clientPhone) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Central heating lead email was skipped because JavaMailSender is not configured");
            return;
        }

        sendSafely(
                mailSender,
                clientEmail,
                "Your " + companyProperties.getName() + " central heating request",
                buildClientEmailBody(state, clientName)
        );

        if (contactProperties.getEmail() != null && !contactProperties.getEmail().isBlank()) {
            sendSafely(
                    mailSender,
                    contactProperties.getEmail(),
                    "New central heating lead",
                    buildBusinessEmailBody(state, serviceType, clientName, clientEmail, clientPhone)
            );
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
            log.error("Failed to send central heating lead email", ex);
        }
    }

    private String buildClientEmailBody(CentralHeatingQuoteSessionState state,
                                        String clientName) {
        return """
                Hello %s,

                Thank you for contacting %s.

                We received your central heating request with the following details:

                Requested work:
                %s

                Valve quantities:
                %s

                Installation items:
                %s

                Other details:
                %s

                We will contact you shortly.
                """.formatted(
                safe(clientName),
                companyProperties.getName(),
                buildRadiatorIssuesSection(state),
                defaultLine(state.getTrvInstallationQuantitySummary(), "- Not requested"),
                buildInstallationItemsSection(state),
                defaultLine(state.getOtherRadiatorIssueDetails(), "- None provided")
        );
    }

    private String buildBusinessEmailBody(CentralHeatingQuoteSessionState state,
                                          String serviceType,
                                          String clientName,
                                          String clientEmail,
                                          String clientPhone) {
        return """
                New central heating lead received.

                Status:
                NEW_LEAD

                Service:
                %s

                Client contact:
                Name: %s
                Email: %s
                Phone: %s

                Client answers:
                Postcode: %s
                Ownership: %s
                Property: %s
                Bedrooms: %s
                Boiler type: %s
                Fuel: %s
                Radiators: %s
                TRV valves: %s
                Power flush: %s
                Magnetic filter: %s
                Requested work: %s
                Valve quantities: %s
                Other radiator issue: %s
                Installation items:
                %s
                """.formatted(
                safe(serviceType),
                safe(clientName),
                clientEmail,
                clientPhone,
                safe(state.getPostcode()),
                safe(state.getOwnership()),
                safe(state.getPropertyType()),
                safe(state.getBedrooms()),
                safe(state.getBoilerType()),
                safe(state.getFuel()),
                safe(state.getRadiatorCountSummary()),
                safe(state.getTrvValveStatusSummary()),
                state.getPowerFlushStatus() != null ? state.getPowerFlushStatus().getLabel() : "-",
                state.getMagneticFilterStatus() != null ? state.getMagneticFilterStatus().getLabel() : "-",
                safe(state.getRadiatorIssuesSummary()),
                defaultLine(state.getTrvInstallationQuantitySummary(), "-"),
                defaultLine(state.getOtherRadiatorIssueDetails(), "-"),
                buildInstallationItemsSection(state)
        );
    }

    private String buildRadiatorIssuesSection(CentralHeatingQuoteSessionState state) {
        if (state.getRadiatorIssues() == null || state.getRadiatorIssues().isEmpty()) {
            return "- No radiator issues selected";
        }

        StringBuilder builder = new StringBuilder();
        state.getRadiatorIssues().forEach(issue -> builder.append("- ").append(issue.getLabel()).append("\n"));
        return builder.toString().trim();
    }

    private String buildInstallationItemsSection(CentralHeatingQuoteSessionState state) {
        if (state.getInstallationItems() == null || state.getInstallationItems().isEmpty()) {
            return "- No radiator or towel rail installation items";
        }

        StringBuilder builder = new StringBuilder();
        for (CentralHeatingInstallationItem item : state.getInstallationItems()) {
            builder.append("- ").append(item.getSummary()).append("\n");
        }
        return builder.toString().trim();
    }

    private String defaultLine(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String safe(Object value) {
        return value == null ? "-" : value.toString();
    }
}
