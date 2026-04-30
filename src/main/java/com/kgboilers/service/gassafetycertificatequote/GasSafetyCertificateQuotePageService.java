package com.kgboilers.service.gassafetycertificatequote;

import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import org.springframework.stereotype.Service;

@Service
public class GasSafetyCertificateQuotePageService {

    public static final String SERVICE = "gas-safety-certificate";
    public static final String BASE_PATH = "/gas-safety-certificate-quote";
    public static final String TITLE = "Boiler Service and Gas Safety Certificate";

    private final QuoteWizardService wizardService;

    public GasSafetyCertificateQuotePageService(QuoteWizardService wizardService) {
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
            return BASE_PATH;
        }

        return step.getPath().replaceFirst("^/quote", BASE_PATH);
    }

    public String redirectToStart() {
        return "redirect:" + BASE_PATH;
    }

    public QuoteStep resolveCurrentStep(String requestUri) {
        return switch (requestUri) {
            case BASE_PATH -> QuoteStep.START;
            case BASE_PATH + "/service-type" -> QuoteStep.SERVICE_TYPE;
            case BASE_PATH + "/boiler-type" -> QuoteStep.BOILER_TYPE;
            case BASE_PATH + "/fuel-type" -> QuoteStep.FUEL_TYPE;
            case BASE_PATH + "/boiler-make" -> QuoteStep.BOILER_MAKE;
            case BASE_PATH + "/gas-appliances" -> QuoteStep.GAS_APPLIANCES;
            case BASE_PATH + "/property-ownership" -> QuoteStep.PROPERTY_OWNERSHIP;
            case BASE_PATH + "/property-type" -> QuoteStep.PROPERTY_TYPE;
            case BASE_PATH + "/summary" -> QuoteStep.SUMMARY;
            default -> null;
        };
    }
}
