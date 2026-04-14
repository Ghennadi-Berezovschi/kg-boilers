package com.kgboilers.controller.boilerinstallationquote;

import com.kgboilers.dto.boilerinstallationquote.*;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.service.boilerinstallationquote.QuoteResponseFactory;
import com.kgboilers.service.boilerinstallationquote.QuoteService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/quote")
public class QuoteWizardApiController {

    private final QuoteService quoteService;
    private final QuoteWizardService wizardService;
    private final QuoteSessionService sessionService;
    private final QuoteResponseFactory responseFactory;

    public QuoteWizardApiController(QuoteService quoteService,
                                    QuoteWizardService wizardService,
                                    QuoteSessionService sessionService,
                                    QuoteResponseFactory responseFactory) {
        this.quoteService = quoteService;
        this.wizardService = wizardService;
        this.sessionService = sessionService;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/start")
    public ResponseEntity<QuoteResponseDto> startQuote(@RequestBody @Valid QuoteRequestPostcodeDto request,
                                                       HttpSession session) {

        String postcode = request.getPostcode().trim().toUpperCase();
        quoteService.startQuote(postcode);

        sessionService.clearState(session);
        QuoteSessionState state = sessionService.getOrCreateState(session);
        QuoteStep nextStep = wizardService.startWizard(state, postcode);
        sessionService.saveState(session, state);

        log.info("Quote started");

        return responseFactory.success(nextStep);
    }

    @PostMapping("/fuel")
    public ResponseEntity<QuoteResponseDto> setFuel(@RequestBody @Valid FuelRequestDto request,
                                                    HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FUEL_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFuel(state, request.getFuel());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/property-ownership")
    public ResponseEntity<QuoteResponseDto> setOwnership(@RequestBody @Valid OwnershipRequestDto request,
                                                         HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateOwnership(state, request.getOwnership());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/property-type")
    public ResponseEntity<QuoteResponseDto> setPropertyType(@RequestBody @Valid PropertyTypeRequestDto request,
                                                            HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.PROPERTY_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updatePropertyType(state, request.getPropertyType());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/bedrooms")
    public ResponseEntity<QuoteResponseDto> setBedrooms(@RequestBody @Valid BedroomsRequestDto request,
                                                        HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BEDROOMS)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBedrooms(state, request.getBedrooms());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/boiler-type")
    public ResponseEntity<QuoteResponseDto> setBoilerType(@RequestBody @Valid BoilerTypeRequestDto request,
                                                          HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerType(state, request.getBoilerType());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/boiler-conversion")
    public ResponseEntity<QuoteResponseDto> setBoilerConversion(@RequestBody @Valid BoilerConversionRequestDto request,
                                                                HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_CONVERSION)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerConversion(state, request.getConversion());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/boiler-position")
    public ResponseEntity<QuoteResponseDto> setBoilerPosition(@RequestBody @Valid BoilerPositionRequestDto request,
                                                              HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_POSITION)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerPosition(state, request.getBoilerPosition());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/boiler-location")
    public ResponseEntity<QuoteResponseDto> setBoilerLocation(@RequestBody @Valid BoilerLocationRequestDto request,
                                                              HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_LOCATION)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerLocation(state, request.getLocation());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/boiler-condition")
    public ResponseEntity<QuoteResponseDto> setBoilerCondition(@RequestBody @Valid BoilerConditionRequestDto request,
                                                               HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_CONDITION)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerCondition(state, request.getBoilerCondition());
        sessionService.saveState(session, state);
        return responseFactory.success(nextStep);
    }

    @PostMapping("/relocation")
    public ResponseEntity<QuoteResponseDto> setRelocation(@RequestBody @Valid RelocationRequestDto request,
                                                          HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.RELOCATION)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateRelocation(state, request.getRelocation());

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    @PostMapping("/relocation-distance")
    public ResponseEntity<QuoteResponseDto> setRelocationDistance(@RequestBody @Valid RelocationDistanceRequestDto request,
                                                                  HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.RELOCATION_DISTANCE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateRelocationDistance(
                state,
                request.getRelocationDistance()
        );

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    @PostMapping("/flue-type")
    public ResponseEntity<QuoteResponseDto> setFlueType(@RequestBody @Valid FlueTypeRequestDto request,
                                                        HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFlueType(
                state,
                request.getFlueType(),
                request.getVerticalFlueType()
        );

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    @PostMapping("/flue-length")
    public ResponseEntity<QuoteResponseDto> setFlueLength(@RequestBody @Valid FlueLengthRequestDto request,
                                                          HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_LENGTH)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFlueLength(state, request.getFlueLength());

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    @PostMapping("/flue-position")
    public ResponseEntity<QuoteResponseDto> setFluePosition(@RequestBody @Valid FluePositionRequestDto request,
                                                            HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_POSITION)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFluePosition(state, request.getFluePosition());

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    @PostMapping("/flue-clearance")
    public ResponseEntity<QuoteResponseDto> setFlueClearance(@RequestBody @Valid FlueClearanceRequestDto request,
                                                             HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_CLEARANCE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFlueClearance(state, request.getFlueClearance());

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    @PostMapping("/flue-property-distance")
    public ResponseEntity<QuoteResponseDto> setFluePropertyDistance(@RequestBody @Valid FluePropertyDistanceRequestDto request,
                                                                    HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_PROPERTY_DISTANCE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFluePropertyDistance(
                state,
                request.getFluePropertyDistance()
        );

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    @PostMapping("/radiator-count")
    public ResponseEntity<QuoteResponseDto> setRadiatorCount(@RequestBody @Valid RadiatorCountRequestDto request,
                                                             HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.RADIATOR_COUNT)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateRadiatorCount(state, request.getRadiatorCount());

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    @PostMapping("/bath-shower-count")
    public ResponseEntity<QuoteResponseDto> setBathShowerCount(@RequestBody @Valid BathShowerCountRequestDto request,
                                                               HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BATH_SHOWER_COUNT)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBathShowerCount(state, request.getBathShowerCount());

        sessionService.saveState(session, state);

        return responseFactory.success(nextStep);
    }

    private ResponseEntity<QuoteResponseDto> sessionExpired() {
        return responseFactory.badRequest("SESSION_EXPIRED", "Please start your quote again");
    }
}
