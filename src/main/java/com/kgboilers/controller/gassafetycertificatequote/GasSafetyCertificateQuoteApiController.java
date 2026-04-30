package com.kgboilers.controller.gassafetycertificatequote;

import com.kgboilers.dto.boilerinstallationquote.BoilerMakeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FuelRequestDto;
import com.kgboilers.dto.boilerinstallationquote.GasAppliancesRequestDto;
import com.kgboilers.dto.boilerinstallationquote.OwnershipRequestDto;
import com.kgboilers.dto.boilerinstallationquote.PropertyTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteRequestPostcodeDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.dto.boilerinstallationquote.ServiceTypeRequestDto;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.GasApplianceSelection;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.service.boilerinstallationquote.QuoteResponseFactory;
import com.kgboilers.service.boilerinstallationquote.QuoteService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import com.kgboilers.service.gassafetycertificatequote.GasSafetyCertificateQuotePageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(GasSafetyCertificateQuotePageService.BASE_PATH)
public class GasSafetyCertificateQuoteApiController {

    private final QuoteService quoteService;
    private final QuoteWizardService wizardService;
    private final QuoteSessionService sessionService;
    private final QuoteResponseFactory responseFactory;
    private final GasSafetyCertificateQuotePageService pageService;

    public GasSafetyCertificateQuoteApiController(QuoteService quoteService,
                                                  QuoteWizardService wizardService,
                                                  QuoteSessionService sessionService,
                                                  QuoteResponseFactory responseFactory,
                                                  GasSafetyCertificateQuotePageService pageService) {
        this.quoteService = quoteService;
        this.wizardService = wizardService;
        this.sessionService = sessionService;
        this.responseFactory = responseFactory;
        this.pageService = pageService;
    }

    @PostMapping("/start")
    public ResponseEntity<QuoteResponseDto> startQuote(@RequestBody @Valid QuoteRequestPostcodeDto request,
                                                       HttpSession session) {
        String postcode = request.getPostcode().trim().toUpperCase();
        quoteService.startQuote(postcode, GasSafetyCertificateQuotePageService.SERVICE);

        session.setAttribute("service", GasSafetyCertificateQuotePageService.SERVICE);
        sessionService.clearState(session);
        QuoteSessionState state = sessionService.getOrCreateState(session);
        QuoteStep nextStep = wizardService.startWizard(state, postcode, GasSafetyCertificateQuotePageService.SERVICE);
        sessionService.saveState(session, state);

        log.info("Gas safety certificate quote started");

        return success(nextStep);
    }

    @PostMapping("/service-type")
    public ResponseEntity<QuoteResponseDto> setServiceType(@RequestBody @Valid ServiceTypeRequestDto request,
                                                           HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.SERVICE_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateGasSafetyServiceType(state, request.getServiceType());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/boiler-type")
    public ResponseEntity<QuoteResponseDto> setBoilerType(@RequestBody @Valid BoilerTypeRequestDto request,
                                                          HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.BOILER_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerType(state, request.getBoilerType(), GasSafetyCertificateQuotePageService.SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/fuel")
    public ResponseEntity<QuoteResponseDto> setFuel(@RequestBody @Valid FuelRequestDto request,
                                                    HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.FUEL_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFuel(state, request.getFuel(), GasSafetyCertificateQuotePageService.SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/boiler-make")
    public ResponseEntity<QuoteResponseDto> setBoilerMake(@RequestBody @Valid BoilerMakeRequestDto request,
                                                          HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.BOILER_MAKE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerMake(state, request.getBoilerMake(), GasSafetyCertificateQuotePageService.SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/gas-appliances")
    public ResponseEntity<QuoteResponseDto> setGasAppliances(@RequestBody @Valid GasAppliancesRequestDto request,
                                                             HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.GAS_APPLIANCES)) {
            return sessionExpired();
        }

        List<GasApplianceSelection> appliances = request.getAppliances().stream()
                .map(item -> new GasApplianceSelection(item.getAppliance(), item.getQuantity()))
                .toList();
        QuoteStep nextStep = wizardService.updateGasAppliances(state, appliances);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/property-ownership")
    public ResponseEntity<QuoteResponseDto> setOwnership(@RequestBody @Valid OwnershipRequestDto request,
                                                         HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateOwnership(state, request.getOwnership());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/property-type")
    public ResponseEntity<QuoteResponseDto> setPropertyType(@RequestBody @Valid PropertyTypeRequestDto request,
                                                            HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.PROPERTY_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updatePropertyType(state, request.getPropertyType(), GasSafetyCertificateQuotePageService.SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    private ResponseEntity<QuoteResponseDto> sessionExpired() {
        return responseFactory.badRequest("SESSION_EXPIRED", "Please start your quote again");
    }

    private ResponseEntity<QuoteResponseDto> success(QuoteStep nextStep) {
        ResponseEntity<QuoteResponseDto> response = responseFactory.success(nextStep);
        QuoteResponseDto body = response.getBody();
        if (body == null) {
            return response;
        }

        body.setNextStep(pageService.pathForStep(nextStep));
        return ResponseEntity.status(response.getStatusCode()).body(body);
    }
}
