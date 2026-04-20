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
        String service = getSelectedService(session);

        String postcode = request.getPostcode().trim().toUpperCase();
        quoteService.startQuote(postcode, service);

        sessionService.clearState(session);
        QuoteSessionState state = sessionService.getOrCreateState(session);
        QuoteStep nextStep = wizardService.startWizard(state, postcode);
        sessionService.saveState(session, state);

        log.info("Quote started");

        return success(nextStep, service);
    }

    private String getSelectedService(HttpSession session) {
        Object service = session.getAttribute("service");
        if (service instanceof String serviceValue && !serviceValue.isBlank()) {
            return serviceValue;
        }

        return "boiler-installation";
    }

    private boolean canAccessStep(QuoteSessionState state, QuoteStep step, String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.canAccessStep(state, step, service);
        }

        return wizardService.canAccessStep(state, step);
    }

    private QuoteStep updatePropertyType(QuoteSessionState state,
                                         PropertyTypeRequestDto request,
                                         String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.updatePropertyType(state, request.getPropertyType(), service);
        }

        return wizardService.updatePropertyType(state, request.getPropertyType());
    }

    private QuoteStep updateBoilerType(QuoteSessionState state,
                                       BoilerTypeRequestDto request,
                                       String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.updateBoilerType(state, request.getBoilerType(), service);
        }

        return wizardService.updateBoilerType(state, request.getBoilerType());
    }

    private QuoteStep updateBoilerConversion(QuoteSessionState state,
                                             BoilerConversionRequestDto request,
                                             String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.updateBoilerConversion(state, request.getConversion(), service);
        }

        return wizardService.updateBoilerConversion(state, request.getConversion());
    }

    private QuoteStep updateBoilerMake(QuoteSessionState state,
                                       BoilerMakeRequestDto request,
                                       String service) {
        return wizardService.updateBoilerMake(state, request.getBoilerMake(), service);
    }

    private QuoteStep updateBoilerFloorLevel(QuoteSessionState state,
                                             BoilerFloorLevelRequestDto request,
                                             String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.updateBoilerFloorLevel(state, request.getFloorLevel(), service);
        }

        return wizardService.updateBoilerFloorLevel(state, request.getFloorLevel());
    }

    private QuoteStep updateBoilerLocation(QuoteSessionState state,
                                           BoilerLocationRequestDto request,
                                           String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.updateBoilerLocation(state, request.getLocation(), service);
        }

        return wizardService.updateBoilerLocation(state, request.getLocation());
    }

    private QuoteStep updateRadiatorCount(QuoteSessionState state,
                                          RadiatorCountRequestDto request,
                                          String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.updateRadiatorCount(state, request.getRadiatorCount(), service);
        }

        return wizardService.updateRadiatorCount(state, request.getRadiatorCount());
    }

    private QuoteStep updateFuel(QuoteSessionState state,
                                 FuelRequestDto request,
                                 String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.updateFuel(state, request.getFuel(), service);
        }

        return wizardService.updateFuel(state, request.getFuel());
    }

    @PostMapping("/fuel")
    public ResponseEntity<QuoteResponseDto> setFuel(@RequestBody @Valid FuelRequestDto request,
                                                    HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FUEL_TYPE, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = updateFuel(state, request, service);
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/property-ownership")
    public ResponseEntity<QuoteResponseDto> setOwnership(@RequestBody @Valid OwnershipRequestDto request,
                                                         HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateOwnership(state, request.getOwnership());
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/property-type")
    public ResponseEntity<QuoteResponseDto> setPropertyType(@RequestBody @Valid PropertyTypeRequestDto request,
                                                            HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.PROPERTY_TYPE, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = updatePropertyType(state, request, service);
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/bedrooms")
    public ResponseEntity<QuoteResponseDto> setBedrooms(@RequestBody @Valid BedroomsRequestDto request,
                                                        HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BEDROOMS, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBedrooms(state, request.getBedrooms());
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/boiler-type")
    public ResponseEntity<QuoteResponseDto> setBoilerType(@RequestBody @Valid BoilerTypeRequestDto request,
                                                          HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_TYPE, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = updateBoilerType(state, request, service);
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/boiler-conversion")
    public ResponseEntity<QuoteResponseDto> setBoilerConversion(@RequestBody @Valid BoilerConversionRequestDto request,
                                                                HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_CONVERSION, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = updateBoilerConversion(state, request, service);
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/boiler-make")
    public ResponseEntity<QuoteResponseDto> setBoilerMake(@RequestBody @Valid BoilerMakeRequestDto request,
                                                          HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_MAKE, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = updateBoilerMake(state, request, service);
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/boiler-position")
    public ResponseEntity<QuoteResponseDto> setBoilerPosition(@RequestBody @Valid BoilerPositionRequestDto request,
                                                              HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_POSITION, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerPosition(state, request.getBoilerPosition());
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/boiler-location")
    public ResponseEntity<QuoteResponseDto> setBoilerLocation(@RequestBody @Valid BoilerLocationRequestDto request,
                                                              HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_LOCATION, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = updateBoilerLocation(state, request, service);
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/boiler-floor-level")
    public ResponseEntity<QuoteResponseDto> setBoilerFloorLevel(@RequestBody @Valid BoilerFloorLevelRequestDto request,
                                                                HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_FLOOR_LEVEL, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = updateBoilerFloorLevel(state, request, service);
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/boiler-condition")
    public ResponseEntity<QuoteResponseDto> setBoilerCondition(@RequestBody @Valid BoilerConditionRequestDto request,
                                                               HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_CONDITION, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerCondition(state, request.getBoilerCondition());
        sessionService.saveState(session, state);
        return success(nextStep, service);
    }

    @PostMapping("/relocation")
    public ResponseEntity<QuoteResponseDto> setRelocation(@RequestBody @Valid RelocationRequestDto request,
                                                          HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.RELOCATION, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateRelocation(state, request.getRelocation());

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/relocation-distance")
    public ResponseEntity<QuoteResponseDto> setRelocationDistance(@RequestBody @Valid RelocationDistanceRequestDto request,
                                                                  HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.RELOCATION_DISTANCE, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateRelocationDistance(
                state,
                request.getRelocationDistance()
        );

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/flue-type")
    public ResponseEntity<QuoteResponseDto> setFlueType(@RequestBody @Valid FlueTypeRequestDto request,
                                                        HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_TYPE, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFlueType(
                state,
                request.getFlueType(),
                request.getVerticalFlueType()
        );

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/flue-length")
    public ResponseEntity<QuoteResponseDto> setFlueLength(@RequestBody @Valid FlueLengthRequestDto request,
                                                          HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_LENGTH, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFlueLength(state, request.getFlueLength());

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/sloped-roof-position")
    public ResponseEntity<QuoteResponseDto> setSlopedRoofPosition(@RequestBody @Valid SlopedRoofPositionRequestDto request,
                                                                  HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.SLOPED_ROOF_POSITION, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateSlopedRoofPosition(state, request.getRoofPosition());

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/flue-position")
    public ResponseEntity<QuoteResponseDto> setFluePosition(@RequestBody @Valid FluePositionRequestDto request,
                                                            HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_POSITION, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFluePosition(state, request.getFluePosition());

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/flue-clearance")
    public ResponseEntity<QuoteResponseDto> setFlueClearance(@RequestBody @Valid FlueClearanceRequestDto request,
                                                             HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_CLEARANCE, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFlueClearance(state, request.getFlueClearance());

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/flue-property-distance")
    public ResponseEntity<QuoteResponseDto> setFluePropertyDistance(@RequestBody @Valid FluePropertyDistanceRequestDto request,
                                                                    HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_PROPERTY_DISTANCE, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFluePropertyDistance(
                state,
                request.getFluePropertyDistance()
        );

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/radiator-count")
    public ResponseEntity<QuoteResponseDto> setRadiatorCount(@RequestBody @Valid RadiatorCountRequestDto request,
                                                             HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.RADIATOR_COUNT, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = updateRadiatorCount(state, request, service);

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    @PostMapping("/bath-shower-count")
    public ResponseEntity<QuoteResponseDto> setBathShowerCount(@RequestBody @Valid BathShowerCountRequestDto request,
                                                               HttpSession session) {

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BATH_SHOWER_COUNT, service)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBathShowerCount(state, request.getBathShowerCount());

        sessionService.saveState(session, state);

        return success(nextStep, service);
    }

    private ResponseEntity<QuoteResponseDto> sessionExpired() {
        return responseFactory.badRequest("SESSION_EXPIRED", "Please start your quote again");
    }

    private ResponseEntity<QuoteResponseDto> success(QuoteStep nextStep, String service) {
        return responseFactory.success(nextStep, service);
    }
}
