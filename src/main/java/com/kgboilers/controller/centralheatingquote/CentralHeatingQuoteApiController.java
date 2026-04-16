package com.kgboilers.controller.centralheatingquote;

import com.kgboilers.dto.boilerinstallationquote.BedroomsRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FuelRequestDto;
import com.kgboilers.dto.boilerinstallationquote.OwnershipRequestDto;
import com.kgboilers.dto.boilerinstallationquote.PropertyTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteRequestPostcodeDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.dto.boilerinstallationquote.RadiatorCountRequestDto;
import com.kgboilers.dto.centralheatingquote.MagneticFilterRequestDto;
import com.kgboilers.dto.centralheatingquote.PowerFlushRequestDto;
import com.kgboilers.dto.centralheatingquote.TrvValveRequestDto;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.service.boilerinstallationquote.QuoteResponseFactory;
import com.kgboilers.service.boilerinstallationquote.QuoteService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteSessionService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteWizardService;
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
@RequestMapping("/central-heating-quote")
public class CentralHeatingQuoteApiController {

    private final QuoteService quoteService;
    private final CentralHeatingQuoteWizardService wizardService;
    private final CentralHeatingQuoteSessionService sessionService;
    private final QuoteResponseFactory responseFactory;

    public CentralHeatingQuoteApiController(QuoteService quoteService,
                                            CentralHeatingQuoteWizardService wizardService,
                                            CentralHeatingQuoteSessionService sessionService,
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
        quoteService.startQuote(postcode, "central-heating");

        sessionService.clearState(session);
        CentralHeatingQuoteSessionState state = sessionService.getOrCreateState(session);
        CentralHeatingQuoteStep nextStep = wizardService.startWizard(state, postcode);
        sessionService.saveState(session, state);

        log.info("Central heating quote started");

        return success(nextStep);
    }

    @PostMapping("/fuel")
    public ResponseEntity<QuoteResponseDto> setFuel(@RequestBody @Valid FuelRequestDto request,
                                                    HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.FUEL_TYPE)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updateFuel(state, request.getFuel());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/radiator-count")
    public ResponseEntity<QuoteResponseDto> setRadiatorCount(@RequestBody @Valid RadiatorCountRequestDto request,
                                                             HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_COUNT)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updateRadiatorCount(state, request.getRadiatorCount());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/trv-valves")
    public ResponseEntity<QuoteResponseDto> setTrvValves(@RequestBody @Valid TrvValveRequestDto request,
                                                         HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.TRV_VALVES)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updateTrvValveStatus(state, request.getTrvValveStatus());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/power-flush")
    public ResponseEntity<QuoteResponseDto> setPowerFlush(@RequestBody @Valid PowerFlushRequestDto request,
                                                          HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.POWER_FLUSH)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updatePowerFlush(state, request.getPowerFlushStatus());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/magnetic-filter")
    public ResponseEntity<QuoteResponseDto> setMagneticFilter(@RequestBody @Valid MagneticFilterRequestDto request,
                                                              HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.MAGNETIC_FILTER)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updateMagneticFilter(state, request.getMagneticFilterStatus());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/property-ownership")
    public ResponseEntity<QuoteResponseDto> setOwnership(@RequestBody @Valid OwnershipRequestDto request,
                                                         HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.PROPERTY_OWNERSHIP)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updateOwnership(state, request.getOwnership());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/property-type")
    public ResponseEntity<QuoteResponseDto> setPropertyType(@RequestBody @Valid PropertyTypeRequestDto request,
                                                            HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.PROPERTY_TYPE)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updatePropertyType(state, request.getPropertyType());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/boiler-type")
    public ResponseEntity<QuoteResponseDto> setBoilerType(@RequestBody @Valid BoilerTypeRequestDto request,
                                                          HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.BOILER_TYPE)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updateBoilerType(state, request.getBoilerType());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    @PostMapping("/bedrooms")
    public ResponseEntity<QuoteResponseDto> setBedrooms(@RequestBody @Valid BedroomsRequestDto request,
                                                        HttpSession session) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.BEDROOMS)) {
            return sessionExpired();
        }

        CentralHeatingQuoteStep nextStep = wizardService.updateBedrooms(state, request.getBedrooms());
        sessionService.saveState(session, state);
        return success(nextStep);
    }

    private ResponseEntity<QuoteResponseDto> success(CentralHeatingQuoteStep nextStep) {
        return ResponseEntity.ok(
                QuoteResponseDto.builder()
                        .success(true)
                        .nextStep(nextStep.getPath())
                        .build()
        );
    }

    private ResponseEntity<QuoteResponseDto> sessionExpired() {
        return responseFactory.badRequest("SESSION_EXPIRED", "Please start your quote again");
    }
}
