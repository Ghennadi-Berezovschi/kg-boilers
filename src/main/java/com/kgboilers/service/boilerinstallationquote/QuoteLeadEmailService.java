package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.QuoteOfferProperties;
import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.config.properties.CompanyProperties;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class QuoteLeadEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ContactProperties contactProperties;
    private final QuoteOfferProperties quoteOfferProperties;
    private final CompanyProperties companyProperties;

    public QuoteLeadEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                 ContactProperties contactProperties,
                                 QuoteOfferProperties quoteOfferProperties,
                                 CompanyProperties companyProperties) {
        this.mailSenderProvider = mailSenderProvider;
        this.contactProperties = contactProperties;
        this.quoteOfferProperties = quoteOfferProperties;
        this.companyProperties = companyProperties;
    }

    public void sendLeadEmails(QuoteSessionState state,
                               String serviceType,
                               String selectedBoiler,
                               int totalPriceGbp,
                               String clientName,
                               String clientEmail,
                               String clientPhone,
                               int relocationPriceGbp,
                               int flueLengthPriceGbp,
                               int fluePositionPriceGbp,
                               int flueClearancePriceGbp,
                               List<QuoteOptionalExtra> selectedOptionalExtras,
                               int optionalExtrasPriceGbp) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Quote lead email was skipped because JavaMailSender is not configured");
            return;
        }

        sendSafely(mailSender,
                clientEmail,
                "Your " + companyProperties.getName() + " quote request",
                buildClientEmailBody(clientName,
                        selectedBoiler,
                        totalPriceGbp,
                        relocationPriceGbp,
                        flueLengthPriceGbp,
                        fluePositionPriceGbp,
                        flueClearancePriceGbp,
                        selectedOptionalExtras,
                        optionalExtrasPriceGbp));

        if (contactProperties.getEmail() != null && !contactProperties.getEmail().isBlank()) {
            sendSafely(mailSender,
                    contactProperties.getEmail(),
                    "New boiler lead: " + selectedBoiler,
                    buildBusinessEmailBody(state,
                            serviceType,
                            selectedBoiler,
                            totalPriceGbp,
                            clientName,
                            clientEmail,
                            clientPhone,
                            relocationPriceGbp,
                            flueLengthPriceGbp,
                            fluePositionPriceGbp,
                            flueClearancePriceGbp,
                            selectedOptionalExtras,
                            optionalExtrasPriceGbp));
        }
    }

    public void sendRepairLeadEmails(QuoteSessionState state,
                                     String serviceType,
                                     String clientName,
                                     String clientEmail,
                                     String clientPhone) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Boiler repair lead email was skipped because JavaMailSender is not configured");
            return;
        }

        sendSafely(mailSender,
                clientEmail,
                "Your " + companyProperties.getName() + " boiler repair request",
                buildRepairClientEmailBody(state, clientName));

        if (contactProperties.getEmail() != null && !contactProperties.getEmail().isBlank()) {
            sendSafely(mailSender,
                    contactProperties.getEmail(),
                    "New boiler repair lead",
                    buildRepairBusinessEmailBody(state, serviceType, clientName, clientEmail, clientPhone));
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

    private String buildRepairClientEmailBody(QuoteSessionState state,
                                              String clientName) {
        return """
                Hello %s,

                Thank you for contacting %s.

                We received your boiler repair request successfully.

                Our team will review the repair information you submitted and use your contact details to prepare the next step for your home.

                We will contact you shortly.
                """.formatted(
                clientName,
                companyProperties.getName()
        );
    }

    private String buildRepairBusinessEmailBody(QuoteSessionState state,
                                                String serviceType,
                                                String clientName,
                                                String clientEmail,
                                                String clientPhone) {
        return """
                New boiler repair lead received.

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
                Fuel: %s
                Ownership: %s
                Property: %s
                Boiler type: %s
                Boiler make: %s
                Boiler age: %s
                Boiler location: %s
                Radiators: %s
                Power flushed in last 5 years: %s
                Magnetic filter: %s
                Not working: %s
                Boiler pressure: %s
                Fault code / message / signal: %s
                Fault code details: %s
                """.formatted(
                stateSafe(serviceType),
                stateSafe(clientName),
                clientEmail,
                clientPhone,
                stateSafe(state.getPostcode()),
                formatRepairValue(state.getFuel()),
                formatRepairValue(state.getOwnership()),
                formatRepairValue(state.getPropertyType()),
                formatRepairValue(state.getBoilerType()),
                formatRepairValue(state.getBoilerMake()),
                defaultLine(state.getBoilerAgeSummary(), "-"),
                formatRepairValue(state.getBoilerLocation()),
                defaultLine(state.getRadiatorCountSummary(), "-"),
                defaultLine(state.getPowerFlushSummary(), "-"),
                defaultLine(state.getMagneticFilterSummary(), "-"),
                defaultLine(state.getRepairProblemSummary(), "-"),
                defaultLine(state.getBoilerPressureSummary(), "-"),
                defaultLine(state.getFaultCodeDisplaySummary(), "-"),
                defaultLine(state.getFaultCodeDetailsSummary(), "-")
        );
    }

    private String buildClientEmailBody(String clientName,
                                        String selectedBoiler,
                                        int totalPriceGbp,
                                        int relocationPriceGbp,
                                        int flueLengthPriceGbp,
                                        int fluePositionPriceGbp,
                                        int flueClearancePriceGbp,
                                        List<QuoteOptionalExtra> selectedOptionalExtras,
                                        int optionalExtrasPriceGbp) {
        return """
                Hello %s,

                Thank you for choosing %s.

                Your fixed price including installation:
                %s

                Boiler price installation:
                £%d

                Included:
                %s

                Selected installation extras:
                %s

                Optional extras:
                %s

                Total selected extras:
                £%d

                We will contact you shortly.
                """.formatted(
                clientName,
                companyProperties.getName(),
                selectedBoiler,
                totalPriceGbp,
                buildIncludedItemsSection(),
                buildSelectedExtrasSection(relocationPriceGbp, flueLengthPriceGbp, fluePositionPriceGbp, flueClearancePriceGbp),
                buildOptionalExtrasSection(selectedOptionalExtras),
                relocationPriceGbp + flueLengthPriceGbp + fluePositionPriceGbp + flueClearancePriceGbp + optionalExtrasPriceGbp
        );
    }

    private String buildBusinessEmailBody(QuoteSessionState state,
                                          String serviceType,
                                          String selectedBoiler,
                                          int totalPriceGbp,
                                          String clientName,
                                          String clientEmail,
                                          String clientPhone,
                                          int relocationPriceGbp,
                                          int flueLengthPriceGbp,
                                          int fluePositionPriceGbp,
                                          int flueClearancePriceGbp,
                                          List<QuoteOptionalExtra> selectedOptionalExtras,
                                          int optionalExtrasPriceGbp) {
        return """
                New boiler lead received.

                Status:
                NEW_LEAD

                Service:
                %s

                Selected boiler:
                %s

                Boiler price installation:
                £%d

                Client contact:
                Name: %s
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
                Boiler floor: %s
                Boiler condition: %s
                Relocation: %s
                Relocation distance: %s
                Relocation price: £%d
                Flue type: %s
                Flue length: %s
                Roof position: %s
                Flue length price: £%d
                Flue position: %s
                Flue position price: £%d
                Flue clearance: %s
                Flue clearance price: £%d
                Flue property distance: %s
                Radiators: %s
                Baths and showers: %s
                Optional extras: %s
                Optional extras price: £%d
                """.formatted(
                stateSafe(serviceType),
                selectedBoiler,
                totalPriceGbp,
                stateSafe(clientName),
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
                stateSafe(state.getBoilerFloorLevel()),
                stateSafe(state.getBoilerCondition()),
                stateSafe(state.getRelocation()),
                state.getRelocationDistanceSummary(),
                relocationPriceGbp,
                state.getFlueSummary(),
                state.getFlueLengthSummary(),
                state.getSlopedRoofPositionSummary(),
                flueLengthPriceGbp,
                state.getFluePositionSummary(),
                fluePositionPriceGbp,
                state.getFlueClearanceSummary(),
                flueClearancePriceGbp,
                state.getFluePropertyDistanceSummary(),
                state.getRadiatorCountSummary(),
                state.getBathShowerCountSummary(),
                buildBusinessOptionalExtrasLine(selectedOptionalExtras),
                optionalExtrasPriceGbp
        );
    }

    private String buildSelectedExtrasSection(int relocationPriceGbp,
                                              int flueLengthPriceGbp,
                                              int fluePositionPriceGbp,
                                              int flueClearancePriceGbp) {
        StringBuilder extras = new StringBuilder();

        appendExtra(extras, "Moving boiler", relocationPriceGbp);
        appendExtra(extras, "Flue extension pack", flueLengthPriceGbp);
        appendExtra(extras, "Balcony or structure flue adjustment", fluePositionPriceGbp);
        appendExtra(extras, "Flue clearance adjustment", flueClearancePriceGbp);

        if (extras.isEmpty()) {
            return "- No extra installation charges selected";
        }

        return extras.toString().trim();
    }

    private String buildIncludedItemsSection() {
        StringBuilder includedItems = new StringBuilder();

        for (String item : quoteOfferProperties.getIncludedItems()) {
            includedItems.append("- ")
                    .append(item)
                    .append("\n");
        }

        if (includedItems.isEmpty()) {
            return "- Boiler installation";
        }

        return includedItems.toString().trim();
    }

    private String buildOptionalExtrasSection(List<QuoteOptionalExtra> selectedOptionalExtras) {
        if (selectedOptionalExtras == null || selectedOptionalExtras.isEmpty()) {
            return "- No optional extras selected";
        }

        StringBuilder optionalExtras = new StringBuilder();
        for (QuoteOptionalExtra extra : selectedOptionalExtras) {
            optionalExtras.append("- ")
                    .append(extra.getTitle())
                    .append(": £")
                    .append(extra.getPriceGbp())
                    .append("\n");
        }

        return optionalExtras.toString().trim();
    }

    private String buildBusinessOptionalExtrasLine(List<QuoteOptionalExtra> selectedOptionalExtras) {
        if (selectedOptionalExtras == null || selectedOptionalExtras.isEmpty()) {
            return "None";
        }

        return selectedOptionalExtras.stream()
                .map(extra -> extra.getTitle() + " (£" + extra.getPriceGbp() + ")")
                .reduce((left, right) -> left + ", " + right)
                .orElse("None");
    }

    private void appendExtra(StringBuilder extras, String label, int priceGbp) {
        if (priceGbp <= 0) {
            return;
        }

        extras.append("- ")
                .append(label)
                .append(": £")
                .append(priceGbp)
                .append("\n");
    }

    private String defaultLine(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatRepairValue(Enum<?> value) {
        if (value == null) {
            return "-";
        }

        try {
            Object raw = value.getClass().getMethod("getValue").invoke(value);
            if (raw instanceof String stringValue && !stringValue.isBlank()) {
                return humanizeValue(stringValue);
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall back to enum name below.
        }

        return humanizeValue(value.name());
    }

    private String humanizeValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "-";
        }

        String normalized = rawValue.replace('_', '-').trim().toLowerCase();
        String[] parts = normalized.split("-");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            if (result.length() > 0) {
                result.append(' ');
            }

            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }

        return result.toString();
    }

    private String stateSafe(Object value) {
        return value == null ? "" : value.toString();
    }
}
