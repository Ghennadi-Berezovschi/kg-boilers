package com.kgboilers.service.boilerrepairquote;

import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class BoilerRepairQuotePageService {

    public static final String SERVICE = "boiler-repair";

    private final QuoteWizardService wizardService;

    public BoilerRepairQuotePageService(QuoteWizardService wizardService) {
        this.wizardService = wizardService;
    }

    public boolean canAccessStep(QuoteSessionState state, QuoteStep step) {
        return wizardService.canAccessStep(state, step, SERVICE);
    }

    public boolean isComplete(QuoteSessionState state) {
        return wizardService.isComplete(state, SERVICE);
    }

    public String pathForStep(QuoteStep step) {
        if (step == QuoteStep.START) {
            return "/boiler-repair-quote";
        }

        return step.getPath().replaceFirst("^/quote", "/boiler-repair-quote");
    }

    public String redirectToStart() {
        return "redirect:" + pathForStep(QuoteStep.START);
    }

    public QuoteStep resolveCurrentStep(String requestUri) {
        return switch (requestUri) {
            case "/boiler-repair-quote" -> QuoteStep.START;
            case "/boiler-repair-quote/fuel-type" -> QuoteStep.FUEL_TYPE;
            case "/boiler-repair-quote/property-ownership" -> QuoteStep.PROPERTY_OWNERSHIP;
            case "/boiler-repair-quote/property-type" -> QuoteStep.PROPERTY_TYPE;
            case "/boiler-repair-quote/boiler-type" -> QuoteStep.BOILER_TYPE;
            case "/boiler-repair-quote/boiler-make" -> QuoteStep.BOILER_MAKE;
            case "/boiler-repair-quote/boiler-age" -> QuoteStep.BOILER_AGE;
            case "/boiler-repair-quote/boiler-location" -> QuoteStep.BOILER_LOCATION;
            case "/boiler-repair-quote/radiator-count" -> QuoteStep.RADIATOR_COUNT;
            case "/boiler-repair-quote/power-flush" -> QuoteStep.POWER_FLUSH;
            case "/boiler-repair-quote/magnetic-filter" -> QuoteStep.MAGNETIC_FILTER;
            case "/boiler-repair-quote/repair-problem" -> QuoteStep.REPAIR_PROBLEM;
            case "/boiler-repair-quote/boiler-pressure" -> QuoteStep.BOILER_PRESSURE;
            case "/boiler-repair-quote/fault-code" -> QuoteStep.FAULT_CODE_DISPLAY;
            case "/boiler-repair-quote/fault-code-details" -> QuoteStep.FAULT_CODE_DETAILS;
            case "/boiler-repair-quote/summary" -> QuoteStep.SUMMARY;
            default -> null;
        };
    }

    public void populateSummaryModel(Model model, QuoteSessionState state) {
        QuoteStep backStep = state != null && state.requiresFaultCodeDetails()
                ? QuoteStep.FAULT_CODE_DETAILS
                : QuoteStep.FAULT_CODE_DISPLAY;

        model.addAttribute("state", state);
        model.addAttribute("backUrl", pathForStep(backStep));
        model.addAttribute("requestTitle", "Boiler Repair");
        model.addAttribute("postcodeValue", defaultLine(state != null ? state.getPostcode() : null));
        model.addAttribute("fuelValue", formatSelection(state != null ? state.getFuel() : null));
        model.addAttribute("ownershipValue", formatSelection(state != null ? state.getOwnership() : null));
        model.addAttribute("propertyTypeValue", formatSelection(state != null ? state.getPropertyType() : null));
        model.addAttribute("boilerTypeValue", formatSelection(state != null ? state.getBoilerType() : null));
        model.addAttribute("boilerMakeValue", formatSelection(state != null ? state.getBoilerMake() : null));
        model.addAttribute("boilerAgeValue", defaultLine(state != null ? state.getBoilerAgeSummary() : null));
        model.addAttribute("boilerLocationValue", formatSelection(state != null ? state.getBoilerLocation() : null));
        model.addAttribute("radiatorCountValue", defaultLine(state != null ? state.getRadiatorCountSummary() : null));
        model.addAttribute("powerFlushValue", defaultLine(state != null ? state.getPowerFlushSummary() : null));
        model.addAttribute("magneticFilterValue", defaultLine(state != null ? state.getMagneticFilterSummary() : null));
        model.addAttribute("repairProblemValue", defaultLine(state != null ? state.getRepairProblemSummary() : null));
        model.addAttribute("boilerPressureValue", defaultLine(state != null ? state.getBoilerPressureSummary() : null));
        model.addAttribute("faultCodeValue", defaultLine(state != null ? state.getFaultCodeDisplaySummary() : null));
        model.addAttribute("faultCodeDetailsValue", defaultLine(state != null ? state.getFaultCodeDetailsSummary() : null));
    }

    private String formatSelection(Enum<?> value) {
        if (value == null) {
            return "-";
        }

        try {
            Object raw = value.getClass().getMethod("getValue").invoke(value);
            if (raw instanceof String stringValue && !stringValue.isBlank()) {
                return humanizeValue(stringValue);
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall back to enum name.
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

    private String defaultLine(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
