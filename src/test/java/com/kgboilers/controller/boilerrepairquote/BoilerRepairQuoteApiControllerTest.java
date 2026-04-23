package com.kgboilers.controller.boilerrepairquote;

import com.kgboilers.dto.boilerinstallationquote.BoilerLocationRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerMakeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FuelRequestDto;
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
import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerrepair.enums.BoilerAge;
import com.kgboilers.model.boilerrepair.enums.BoilerPressureStatus;
import com.kgboilers.model.boilerrepair.enums.FaultCodeDisplayStatus;
import com.kgboilers.model.boilerrepair.enums.MagneticFilterStatus;
import com.kgboilers.model.boilerrepair.enums.PowerFlushStatus;
import com.kgboilers.model.boilerrepair.enums.RepairProblem;
import com.kgboilers.service.boilerinstallationquote.QuoteResponseFactory;
import com.kgboilers.service.boilerinstallationquote.QuoteService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BoilerRepairQuoteApiControllerTest {

    private QuoteService quoteService;
    private QuoteWizardService wizardService;
    private QuoteSessionService sessionService;
    private HttpSession session;
    private BoilerRepairQuoteApiController controller;

    @BeforeEach
    void setUp() {
        quoteService = mock(QuoteService.class);
        wizardService = mock(QuoteWizardService.class);
        sessionService = mock(QuoteSessionService.class);
        session = mock(HttpSession.class);

        controller = new BoilerRepairQuoteApiController(
                quoteService,
                wizardService,
                sessionService,
                new QuoteResponseFactory()
        );
    }

    @Test
    void startQuote_shouldReturnRepairFuelType_andSetServiceInSession() {
        when(quoteService.startQuote(anyString(), anyString())).thenReturn(QuoteStep.FUEL_TYPE);
        when(sessionService.getOrCreateState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.startWizard(any(), anyString())).thenReturn(QuoteStep.FUEL_TYPE);

        QuoteRequestPostcodeDto request = new QuoteRequestPostcodeDto();
        request.setPostcode("E16 4JJ");

        ResponseEntity<QuoteResponseDto> response = controller.startQuote(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/fuel-type", response.getBody().getNextStep());
        verify(session).setAttribute("service", "boiler-repair");
        verify(sessionService).saveState(eq(session), any(QuoteSessionState.class));
    }

    @Test
    void setFuel_shouldReturnPropertyOwnershipForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FUEL_TYPE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FUEL_TYPE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateFuel(state, FuelType.ELECTRIC, "boiler-repair")).thenReturn(QuoteStep.PROPERTY_OWNERSHIP);

        FuelRequestDto request = new FuelRequestDto();
        request.setFuel(FuelType.ELECTRIC);

        ResponseEntity<QuoteResponseDto> response = controller.setFuel(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/property-ownership", response.getBody().getNextStep());
    }

    @Test
    void setBoilerType_shouldReturnBoilerMakeForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_TYPE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_TYPE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerType(state, BoilerType.COMBI, "boiler-repair")).thenReturn(QuoteStep.BOILER_MAKE);

        BoilerTypeRequestDto request = new BoilerTypeRequestDto();
        request.setBoilerType(BoilerType.COMBI);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerType(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/boiler-make", response.getBody().getNextStep());
    }

    @Test
    void setBoilerMake_shouldReturnBoilerAgeForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_MAKE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_MAKE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerMake(state, BoilerMake.WORCESTER_BOSCH, "boiler-repair")).thenReturn(QuoteStep.BOILER_AGE);

        BoilerMakeRequestDto request = new BoilerMakeRequestDto();
        request.setBoilerMake(BoilerMake.WORCESTER_BOSCH);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerMake(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/boiler-age", response.getBody().getNextStep());
    }

    @Test
    void setBoilerAge_shouldReturnBoilerLocationForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_AGE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_AGE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerAge(state, BoilerAge.TWO_TO_FIVE_YEARS, "boiler-repair")).thenReturn(QuoteStep.BOILER_LOCATION);

        BoilerAgeRequestDto request = new BoilerAgeRequestDto();
        request.setBoilerAge(BoilerAge.TWO_TO_FIVE_YEARS);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerAge(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/boiler-location", response.getBody().getNextStep());
    }

    @Test
    void setBoilerLocation_shouldReturnRadiatorCountForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_LOCATION);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_LOCATION, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerLocation(state, BoilerLocation.KITCHEN, "boiler-repair")).thenReturn(QuoteStep.RADIATOR_COUNT);

        BoilerLocationRequestDto request = new BoilerLocationRequestDto();
        request.setLocation(BoilerLocation.KITCHEN);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerLocation(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/radiator-count", response.getBody().getNextStep());
    }

    @Test
    void setRadiatorCount_shouldReturnPowerFlushForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.RADIATOR_COUNT);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.RADIATOR_COUNT, "boiler-repair")).thenReturn(true);
        when(wizardService.updateRadiatorCount(state, RadiatorCount.SIX_TO_NINE, "boiler-repair")).thenReturn(QuoteStep.POWER_FLUSH);

        RadiatorCountRequestDto request = new RadiatorCountRequestDto();
        request.setRadiatorCount(RadiatorCount.SIX_TO_NINE);

        ResponseEntity<QuoteResponseDto> response = controller.setRadiatorCount(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/power-flush", response.getBody().getNextStep());
    }

    @Test
    void setPowerFlush_shouldReturnMagneticFilterForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.POWER_FLUSH);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.POWER_FLUSH, "boiler-repair")).thenReturn(true);
        when(wizardService.updatePowerFlush(state, PowerFlushStatus.YES_DONE, "boiler-repair")).thenReturn(QuoteStep.MAGNETIC_FILTER);

        PowerFlushRequestDto request = new PowerFlushRequestDto();
        request.setPowerFlushStatus(PowerFlushStatus.YES_DONE);

        ResponseEntity<QuoteResponseDto> response = controller.setPowerFlush(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/magnetic-filter", response.getBody().getNextStep());
    }

    @Test
    void setMagneticFilter_shouldReturnRepairProblemForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.MAGNETIC_FILTER);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.MAGNETIC_FILTER, "boiler-repair")).thenReturn(true);
        when(wizardService.updateMagneticFilter(state, MagneticFilterStatus.YES_HAS, "boiler-repair")).thenReturn(QuoteStep.REPAIR_PROBLEM);

        MagneticFilterRequestDto request = new MagneticFilterRequestDto();
        request.setMagneticFilterStatus(MagneticFilterStatus.YES_HAS);

        ResponseEntity<QuoteResponseDto> response = controller.setMagneticFilter(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/repair-problem", response.getBody().getNextStep());
    }

    @Test
    void setRepairProblem_shouldReturnBoilerPressureForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.REPAIR_PROBLEM);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.REPAIR_PROBLEM, "boiler-repair")).thenReturn(true);
        when(wizardService.updateRepairProblem(state, RepairProblem.HEATING, "boiler-repair")).thenReturn(QuoteStep.BOILER_PRESSURE);

        RepairProblemRequestDto request = new RepairProblemRequestDto();
        request.setRepairProblem(RepairProblem.HEATING);

        ResponseEntity<QuoteResponseDto> response = controller.setRepairProblem(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/boiler-pressure", response.getBody().getNextStep());
    }

    @Test
    void setBoilerPressure_shouldReturnFaultCodeForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_PRESSURE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_PRESSURE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerPressure(state, BoilerPressureStatus.YES_DROPPED_OR_DROPPING, "boiler-repair")).thenReturn(QuoteStep.FAULT_CODE_DISPLAY);

        BoilerPressureRequestDto request = new BoilerPressureRequestDto();
        request.setBoilerPressureStatus(BoilerPressureStatus.YES_DROPPED_OR_DROPPING);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerPressure(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/fault-code", response.getBody().getNextStep());
    }

    @Test
    void setFaultCodeDisplay_shouldReturnDetailsForRepair_whenShowing() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FAULT_CODE_DISPLAY);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FAULT_CODE_DISPLAY, "boiler-repair")).thenReturn(true);
        when(wizardService.updateFaultCodeDisplay(state, FaultCodeDisplayStatus.YES_SHOWING, "boiler-repair")).thenReturn(QuoteStep.FAULT_CODE_DETAILS);

        FaultCodeDisplayRequestDto request = new FaultCodeDisplayRequestDto();
        request.setFaultCodeDisplayStatus(FaultCodeDisplayStatus.YES_SHOWING);

        ResponseEntity<QuoteResponseDto> response = controller.setFaultCodeDisplay(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/fault-code-details", response.getBody().getNextStep());
    }

    @Test
    void setFaultCodeDisplay_shouldReturnSummaryForRepair_whenNothingShowing() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FAULT_CODE_DISPLAY);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FAULT_CODE_DISPLAY, "boiler-repair")).thenReturn(true);
        when(wizardService.updateFaultCodeDisplay(state, FaultCodeDisplayStatus.NO_NOT_SHOWING, "boiler-repair")).thenReturn(QuoteStep.SUMMARY);

        FaultCodeDisplayRequestDto request = new FaultCodeDisplayRequestDto();
        request.setFaultCodeDisplayStatus(FaultCodeDisplayStatus.NO_NOT_SHOWING);

        ResponseEntity<QuoteResponseDto> response = controller.setFaultCodeDisplay(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/summary", response.getBody().getNextStep());
    }

    @Test
    void setFaultCodeDetails_shouldReturnSummaryForRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FAULT_CODE_DETAILS);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FAULT_CODE_DETAILS, "boiler-repair")).thenReturn(true);
        when(wizardService.updateFaultCodeDetails(state, "F22 low pressure warning", "boiler-repair")).thenReturn(QuoteStep.SUMMARY);

        FaultCodeDetailsRequestDto request = new FaultCodeDetailsRequestDto();
        request.setFaultCodeDetails("F22 low pressure warning");

        ResponseEntity<QuoteResponseDto> response = controller.setFaultCodeDetails(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/boiler-repair-quote/summary", response.getBody().getNextStep());
    }
}
