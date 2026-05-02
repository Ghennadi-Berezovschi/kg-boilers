package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.QuoteOfferProperties;
import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.config.properties.CompanyProperties;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallationquote.UploadedPicture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class QuoteLeadEmailService {

    private static final String HOT_WATER_CYLINDER_SERVICE = "hot-water-cylinder";
    private static final String HOT_WATER_CYLINDER_TITLE = "Hot Water Cylinder Installation & Repair";

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

    @Async("leadEmailTaskExecutor")
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
                               int horizontalFlueShapePriceGbp,
                               int heatOnlyConversionPriceGbp,
                               List<QuoteOptionalExtra> selectedOptionalExtras,
                               int optionalExtrasPriceGbp) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Quote lead email was skipped because JavaMailSender is not configured");
            return;
        }

        if (isHotWaterCylinder(serviceType)) {
            sendSafely(mailSender,
                    clientEmail,
                    "Your " + companyProperties.getName() + " hot water cylinder request",
                    buildHotWaterCylinderClientEmailBody(state, clientName));

            if (contactProperties.getEmail() != null && !contactProperties.getEmail().isBlank()) {
                sendSafely(mailSender,
                        contactProperties.getEmail(),
                        "New hot water cylinder lead",
                        buildHotWaterCylinderBusinessEmailBody(state, serviceType, clientName, clientEmail, clientPhone));
            }
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
                        horizontalFlueShapePriceGbp,
                        heatOnlyConversionPriceGbp,
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
                            horizontalFlueShapePriceGbp,
                            heatOnlyConversionPriceGbp,
                            selectedOptionalExtras,
                            optionalExtrasPriceGbp));
        }
    }

    private String buildHotWaterCylinderClientEmailBody(QuoteSessionState state, String clientName) {
        return """
                Hello %s,

                Thank you for choosing %s.

                We received your hot water cylinder request.
                We will review your details and call you back to confirm the right option.

                %s
                """.formatted(
                clientName,
                companyProperties.getName(),
                companyProperties.getName()
        );
    }

    private String buildHotWaterCylinderBusinessEmailBody(QuoteSessionState state,
                                                          String serviceType,
                                                          String clientName,
                                                          String clientEmail,
                                                          String clientPhone) {
        return """
                New hot water cylinder lead received.

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
                Boiler type: %s
                Boiler make: %s
                Hot water: %s
                Problem: %s
                Uploaded pictures: %s
                """.formatted(
                HOT_WATER_CYLINDER_TITLE,
                stateSafe(clientName),
                clientEmail,
                clientPhone,
                stateSafe(state == null ? null : state.getPostcode()),
                stateSafe(state == null ? null : state.getOwnership()),
                stateSafe(state == null ? null : state.getPropertyType()),
                stateSafe(state == null ? null : state.getBoilerType()),
                stateSafe(state == null ? null : state.getBoilerMake()),
                formatHotWaterAnswer(state),
                state == null ? "" : state.getProblemDetailsSummary(),
                formatUploadedPictures(state)
        );
    }

    @Async("leadEmailTaskExecutor")
    public void sendRepairLeadEmails(QuoteSessionState state,
                                     String serviceType,
                                     String clientName,
                                     String clientEmail,
                                     String clientPhone,
                                     List<String> selectedExtras) {
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
                    buildRepairBusinessEmailBody(state, serviceType, clientName, clientEmail, clientPhone, selectedExtras));
        }
    }

    @Async("leadEmailTaskExecutor")
    public void sendServiceLeadEmails(QuoteSessionState state,
                                      String serviceTitle,
                                      String clientName,
                                      String clientEmail,
                                      String clientPhone,
                                      List<String> selectedExtras) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Service lead email was skipped because JavaMailSender is not configured");
            return;
        }

        String clientBody = """
                Hi %s,

                Thanks, we received your request for %s.

                We will review your details and contact you soon.

                %s
                """.formatted(clientName, serviceTitle, companyProperties.getName());

        String businessBody = """
                New service lead

                Service: %s
                Name: %s
                Email: %s
                Phone: %s
                Postcode: %s
                Gas appliances: %s
                Optional extras: %s
                Uploaded pictures: %s
                """.formatted(
                serviceTitle,
                clientName,
                clientEmail,
                clientPhone,
                state == null ? "" : state.getPostcode(),
                state == null ? "" : state.getGasAppliancesSummary(),
                formatServiceExtras(selectedExtras),
                formatUploadedPictures(state)
        );

        sendSafely(mailSender,
                clientEmail,
                "Your " + companyProperties.getName() + " request",
                clientBody);

        if (contactProperties.getEmail() != null && !contactProperties.getEmail().isBlank()) {
            sendSafely(mailSender,
                    contactProperties.getEmail(),
                    "New service lead: " + serviceTitle,
                    businessBody);
        }
    }

    private void sendSafely(JavaMailSender mailSender,
                            String to,
                            String subject,
                            String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            if (contactProperties.getEmail() != null && !contactProperties.getEmail().isBlank()) {
                message.setFrom(contactProperties.getEmail());
                message.setReplyTo(contactProperties.getEmail());
            }
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send quote lead email", ex);
        }
    }

    private String formatServiceExtras(List<String> selectedExtras) {
        if (selectedExtras == null || selectedExtras.isEmpty()) {
            return "-";
        }

        return String.join(", ", selectedExtras);
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
                                                String clientPhone,
                                                List<String> selectedExtras) {
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
                Boiler type: %s
                Boiler make: %s
                Boiler age: %s
                Not working: %s
                Fault code / message / signal: %s
                Fault code details: %s
                Optional extras: %s
                Uploaded pictures: %s
                """.formatted(
                stateSafe(serviceType),
                stateSafe(clientName),
                clientEmail,
                clientPhone,
                stateSafe(state.getPostcode()),
                formatRepairValue(state.getFuel()),
                formatRepairValue(state.getBoilerType()),
                formatRepairValue(state.getBoilerMake()),
                defaultLine(state.getBoilerAgeSummary(), "-"),
                defaultLine(state.getRepairProblemSummary(), "-"),
                defaultLine(state.getFaultCodeDisplaySummary(), "-"),
                defaultLine(state.getFaultCodeDetailsSummary(), "-"),
                formatServiceExtras(selectedExtras),
                formatUploadedPictures(state)
        );
    }

    private String buildClientEmailBody(String clientName,
                                        String selectedBoiler,
                                        int totalPriceGbp,
                                        int relocationPriceGbp,
                                        int flueLengthPriceGbp,
                                        int fluePositionPriceGbp,
                                        int flueClearancePriceGbp,
                                        int horizontalFlueShapePriceGbp,
                                        int heatOnlyConversionPriceGbp,
                                        List<QuoteOptionalExtra> selectedOptionalExtras,
                                        int optionalExtrasPriceGbp) {
        return """
                Hello %s,

                Thank you for choosing %s.

                Your price including installation:
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
                buildSelectedExtrasSection(relocationPriceGbp, flueLengthPriceGbp, fluePositionPriceGbp, flueClearancePriceGbp, horizontalFlueShapePriceGbp, heatOnlyConversionPriceGbp),
                buildOptionalExtrasSection(selectedOptionalExtras),
                relocationPriceGbp + flueLengthPriceGbp + fluePositionPriceGbp + flueClearancePriceGbp + horizontalFlueShapePriceGbp + heatOnlyConversionPriceGbp + optionalExtrasPriceGbp
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
                                          int horizontalFlueShapePriceGbp,
                                          int heatOnlyConversionPriceGbp,
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
                Horizontal flue shape: %s
                Horizontal flue shape price: £%d
                Flue length: %s
                Roof position: %s
                Flue length price: £%d
                Flue position: %s
                Flue position price: £%d
                Flue clearance: %s
                Flue clearance price: £%d
                Heat Only to Combi conversion price: £%d
                Flue property distance: %s
                Radiators: %s
                Baths and showers: %s
                Optional extras: %s
                Optional extras price: £%d
                Uploaded pictures: %s
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
                state.getHorizontalFlueShapeSummary(),
                horizontalFlueShapePriceGbp,
                state.getFlueLengthSummary(),
                state.getSlopedRoofPositionSummary(),
                flueLengthPriceGbp,
                state.getFluePositionSummary(),
                fluePositionPriceGbp,
                state.getFlueClearanceSummary(),
                flueClearancePriceGbp,
                heatOnlyConversionPriceGbp,
                state.getFluePropertyDistanceSummary(),
                state.getRadiatorCountSummary(),
                state.getBathShowerCountSummary(),
                buildBusinessOptionalExtrasLine(selectedOptionalExtras),
                optionalExtrasPriceGbp,
                formatUploadedPictures(state)
        );
    }

    private String formatUploadedPictures(QuoteSessionState state) {
        if (state == null || state.getUploadedPictures() == null || state.getUploadedPictures().isEmpty()) {
            return "-";
        }

        return state.getUploadedPictures().stream()
                .map(this::formatUploadedPicture)
                .reduce((left, right) -> left + ", " + right)
                .orElse("-");
    }

    private String formatUploadedPicture(UploadedPicture picture) {
        if (picture == null) {
            return "-";
        }

        String label = picture.getOriginalFilename() == null || picture.getOriginalFilename().isBlank()
                ? "picture"
                : picture.getOriginalFilename();
        return label + " (" + picture.getUrl() + ")";
    }

    private String buildSelectedExtrasSection(int relocationPriceGbp,
                                              int flueLengthPriceGbp,
                                              int fluePositionPriceGbp,
                                              int flueClearancePriceGbp,
                                              int horizontalFlueShapePriceGbp,
                                              int heatOnlyConversionPriceGbp) {
        StringBuilder extras = new StringBuilder();

        appendExtra(extras, "Heat Only to Combi conversion", heatOnlyConversionPriceGbp);
        appendExtra(extras, "Square flue terminal adjustment", horizontalFlueShapePriceGbp);
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
                    .append(formatQuantity(extra))
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
                .map(extra -> extra.getTitle() + formatQuantity(extra) + " (£" + extra.getPriceGbp() + ")")
                .reduce((left, right) -> left + ", " + right)
                .orElse("None");
    }

    private String formatQuantity(QuoteOptionalExtra extra) {
        int quantity = extra.getQuantity() == null ? 1 : extra.getQuantity();
        return quantity > 1 ? " x" + quantity : "";
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

    private boolean isHotWaterCylinder(String serviceType) {
        return HOT_WATER_CYLINDER_SERVICE.equalsIgnoreCase(serviceType == null ? "" : serviceType.trim());
    }

    private String formatHotWaterAnswer(QuoteSessionState state) {
        if (state == null || state.getHotWaterAvailable() == null) {
            return "";
        }

        return state.getHotWaterAvailable() ? "Yes" : "No";
    }

    private String stateSafe(Object value) {
        return value == null ? "" : value.toString();
    }
}
