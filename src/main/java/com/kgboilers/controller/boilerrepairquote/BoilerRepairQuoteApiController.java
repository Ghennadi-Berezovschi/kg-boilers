package com.kgboilers.controller.boilerrepairquote;

import com.kgboilers.dto.boilerinstallationquote.BoilerLocationRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerMakeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FuelRequestDto;
import com.kgboilers.dto.boilerinstallationquote.OwnershipRequestDto;
import com.kgboilers.dto.boilerinstallationquote.PropertyTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteRequestPostcodeDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.dto.boilerinstallationquote.RadiatorCountRequestDto;
import com.kgboilers.dto.boilerrepairquote.BoilerAgeRequestDto;
import com.kgboilers.dto.boilerrepairquote.BoilerPressureRequestDto;
import com.kgboilers.dto.boilerrepairquote.FaultCodeDetailsRequestDto;
import com.kgboilers.dto.boilerrepairquote.FaultCodeDisplayRequestDto;
import com.kgboilers.dto.boilerrepairquote.MagneticFilterRequestDto;
import com.kgboilers.dto.boilerrepairquote.PowerFlushRequestDto;
import com.kgboilers.dto.boilerrepairquote.RepairProblemRequestDto;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
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
@RequestMapping("/boiler-repair-quote")
public class BoilerRepairQuoteApiController {

    private static final String SERVICE = "boiler-repair";

    private final QuoteService quoteService;
    private final QuoteWizardService wizardService;
    private final QuoteSessionService sessionService;
    private final QuoteResponseFactory responseFactory;

    public BoilerRepairQuoteApiController(QuoteService quoteService,
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
        quoteService.startQuote(postcode, SERVICE);

        session.setAttribute("service", SERVICE);
        sessionService.clearState(session);
        QuoteSessionState state = sessionService.getOrCreateState(session);
        QuoteStep nextStep = wizardService.startWizard(state, postcode);
        sessionService.saveState(session, state);

        log.info("Boiler repair quote started");

        return success(nextStep);
    }

    @PostMapping("/fuel")
    public ResponseEntity<QuoteResponseDto> setFuel(@RequestBody @Valid FuelRequestDto request,
                                                    HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.FUEL_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFuel(state, request.getFuel(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/property-ownership")
    public ResponseEntity<QuoteResponseDto> setOwnership(@RequestBody @Valid OwnershipRequestDto request,
                                                         HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP)) {
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
        if (!canAccessStep(state, QuoteStep.PROPERTY_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updatePropertyType(state, request.getPropertyType(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/boiler-type")
    public ResponseEntity<QuoteResponseDto> setBoilerType(@RequestBody @Valid BoilerTypeRequestDto request,
                                                          HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.BOILER_TYPE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerType(state, request.getBoilerType(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/boiler-make")
    public ResponseEntity<QuoteResponseDto> setBoilerMake(@RequestBody @Valid BoilerMakeRequestDto request,
                                                          HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.BOILER_MAKE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerMake(state, request.getBoilerMake(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/boiler-age")
    public ResponseEntity<QuoteResponseDto> setBoilerAge(@RequestBody @Valid BoilerAgeRequestDto request,
                                                         HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.BOILER_AGE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerAge(state, request.getBoilerAge(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/boiler-location")
    public ResponseEntity<QuoteResponseDto> setBoilerLocation(@RequestBody @Valid BoilerLocationRequestDto request,
                                                              HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.BOILER_LOCATION)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerLocation(state, request.getLocation(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/radiator-count")
    public ResponseEntity<QuoteResponseDto> setRadiatorCount(@RequestBody @Valid RadiatorCountRequestDto request,
                                                             HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.RADIATOR_COUNT)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateRadiatorCount(state, request.getRadiatorCount(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/power-flush")
    public ResponseEntity<QuoteResponseDto> setPowerFlush(@RequestBody @Valid PowerFlushRequestDto request,
                                                          HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.POWER_FLUSH)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updatePowerFlush(state, request.getPowerFlushStatus(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/magnetic-filter")
    public ResponseEntity<QuoteResponseDto> setMagneticFilter(@RequestBody @Valid MagneticFilterRequestDto request,
                                                              HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.MAGNETIC_FILTER)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateMagneticFilter(state, request.getMagneticFilterStatus(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/repair-problem")
    public ResponseEntity<QuoteResponseDto> setRepairProblem(@RequestBody @Valid RepairProblemRequestDto request,
                                                             HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.REPAIR_PROBLEM)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateRepairProblem(state, request.getRepairProblem(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/boiler-pressure")
    public ResponseEntity<QuoteResponseDto> setBoilerPressure(@RequestBody @Valid BoilerPressureRequestDto request,
                                                              HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.BOILER_PRESSURE)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateBoilerPressure(state, request.getBoilerPressureStatus(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/fault-code")
    public ResponseEntity<QuoteResponseDto> setFaultCodeDisplay(@RequestBody @Valid FaultCodeDisplayRequestDto request,
                                                                HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.FAULT_CODE_DISPLAY)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFaultCodeDisplay(state, request.getFaultCodeDisplayStatus(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/fault-code-details")
    public ResponseEntity<QuoteResponseDto> setFaultCodeDetails(@RequestBody @Valid FaultCodeDetailsRequestDto request,
                                                                HttpSession session) {
        QuoteSessionState state = sessionService.getState(session);
        if (!canAccessStep(state, QuoteStep.FAULT_CODE_DETAILS)) {
            return sessionExpired();
        }

        QuoteStep nextStep = wizardService.updateFaultCodeDetails(state, request.getFaultCodeDetails(), SERVICE);
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    private boolean canAccessStep(QuoteSessionState state, QuoteStep step) {
        return wizardService.canAccessStep(state, step, SERVICE);
    }

    private ResponseEntity<QuoteResponseDto> sessionExpired() {
        return responseFactory.badRequest("SESSION_EXPIRED", "Please start your quote again");
    }

    private ResponseEntity<QuoteResponseDto> success(QuoteStep nextStep) {
        return responseFactory.success(nextStep, SERVICE);
    }
}
