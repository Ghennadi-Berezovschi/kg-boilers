package com.kgboilers.controller.centralheatingquote;

import com.kgboilers.dto.boilerinstallationquote.BoilerTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FuelRequestDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteRequestPostcodeDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.dto.boilerinstallationquote.RadiatorCountRequestDto;
import com.kgboilers.dto.centralheatingquote.MagneticFilterRequestDto;
import com.kgboilers.dto.centralheatingquote.PowerFlushRequestDto;
import com.kgboilers.dto.centralheatingquote.TrvValveRequestDto;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.MagneticFilterStatus;
import com.kgboilers.model.centralheatingquote.enums.PowerFlushStatus;
import com.kgboilers.model.centralheatingquote.enums.TrvValveStatus;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.service.boilerinstallationquote.QuoteResponseFactory;
import com.kgboilers.service.boilerinstallationquote.QuoteService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteSessionService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteWizardService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CentralHeatingQuoteApiControllerTest {

    private QuoteService quoteService;
    private CentralHeatingQuoteWizardService wizardService;
    private CentralHeatingQuoteSessionService sessionService;
    private HttpSession session;
    private CentralHeatingQuoteApiController controller;

    @BeforeEach
    void setUp() {
        quoteService = mock(QuoteService.class);
        wizardService = mock(CentralHeatingQuoteWizardService.class);
        sessionService = mock(CentralHeatingQuoteSessionService.class);
        session = mock(HttpSession.class);

        controller = new CentralHeatingQuoteApiController(
                quoteService,
                wizardService,
                sessionService,
                new QuoteResponseFactory()
        );
    }

    @Test
    void startQuote_shouldUseCentralHeatingServiceLimitAndRedirectToFuelType() {
        when(sessionService.getOrCreateState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.startWizard(any(), anyString())).thenReturn(CentralHeatingQuoteStep.PROPERTY_OWNERSHIP);

        QuoteRequestPostcodeDto request = new QuoteRequestPostcodeDto();
        request.setPostcode("E16 4JJ");

        ResponseEntity<QuoteResponseDto> response = controller.startQuote(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/property-ownership", response.getBody().getNextStep());

        verify(quoteService).startQuote("E16 4JJ", "central-heating");
        verify(sessionService).clearState(session);
        verify(sessionService).saveState(eq(session), any(CentralHeatingQuoteSessionState.class));
    }

    @Test
    void setBoilerType_shouldRedirectToBedrooms() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.BOILER_TYPE)).thenReturn(true);
        when(wizardService.updateBoilerType(state, BoilerType.COMBI)).thenReturn(CentralHeatingQuoteStep.FUEL_TYPE);

        BoilerTypeRequestDto request = new BoilerTypeRequestDto();
        request.setBoilerType(BoilerType.COMBI);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerType(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/fuel-type", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setFuel_shouldAllowLpgAndRedirectToRadiatorCount() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.FUEL_TYPE)).thenReturn(true);
        when(wizardService.updateFuel(state, FuelType.LPG)).thenReturn(CentralHeatingQuoteStep.RADIATOR_COUNT);

        FuelRequestDto request = new FuelRequestDto();
        request.setFuel(FuelType.LPG);

        ResponseEntity<QuoteResponseDto> response = controller.setFuel(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/radiator-count", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setRadiatorCount_shouldRedirectToPowerFlush() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_COUNT)).thenReturn(true);
        when(wizardService.updateRadiatorCount(state, RadiatorCount.TEN_TO_THIRTEEN)).thenReturn(CentralHeatingQuoteStep.TRV_VALVES);

        RadiatorCountRequestDto request = new RadiatorCountRequestDto();
        request.setRadiatorCount(RadiatorCount.TEN_TO_THIRTEEN);

        ResponseEntity<QuoteResponseDto> response = controller.setRadiatorCount(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/trv-valves", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setTrvValves_shouldRedirectToPowerFlush() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.TRV_VALVES)).thenReturn(true);
        when(wizardService.updateTrvValveStatus(state, TrvValveStatus.NOT_ALL_OF_THEM)).thenReturn(CentralHeatingQuoteStep.POWER_FLUSH);

        TrvValveRequestDto request = new TrvValveRequestDto();
        request.setTrvValveStatus(TrvValveStatus.NOT_ALL_OF_THEM);

        ResponseEntity<QuoteResponseDto> response = controller.setTrvValves(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/power-flush", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setPowerFlush_shouldRedirectToComingSoon() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.POWER_FLUSH)).thenReturn(true);
        when(wizardService.updatePowerFlush(state, PowerFlushStatus.YES_DONE)).thenReturn(CentralHeatingQuoteStep.MAGNETIC_FILTER);

        PowerFlushRequestDto request = new PowerFlushRequestDto();
        request.setPowerFlushStatus(PowerFlushStatus.YES_DONE);

        ResponseEntity<QuoteResponseDto> response = controller.setPowerFlush(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/magnetic-filter", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setMagneticFilter_shouldRedirectToComingSoon() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.MAGNETIC_FILTER)).thenReturn(true);
        when(wizardService.updateMagneticFilter(state, MagneticFilterStatus.YES_HAS)).thenReturn(CentralHeatingQuoteStep.COMING_SOON);

        MagneticFilterRequestDto request = new MagneticFilterRequestDto();
        request.setMagneticFilterStatus(MagneticFilterStatus.YES_HAS);

        ResponseEntity<QuoteResponseDto> response = controller.setMagneticFilter(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/coming-soon", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }
}
