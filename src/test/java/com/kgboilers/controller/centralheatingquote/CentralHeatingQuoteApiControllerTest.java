package com.kgboilers.controller.centralheatingquote;

import com.kgboilers.dto.boilerinstallationquote.BoilerTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FuelRequestDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteRequestPostcodeDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.dto.boilerinstallationquote.RadiatorCountRequestDto;
import com.kgboilers.dto.centralheatingquote.MagneticFilterRequestDto;
import com.kgboilers.dto.centralheatingquote.PowerFlushRequestDto;
import com.kgboilers.dto.centralheatingquote.RadiatorIssuesRequestDto;
import com.kgboilers.dto.centralheatingquote.RadiatorSpecificationRequestDto;
import com.kgboilers.dto.centralheatingquote.TrvInstallationQuantityRequestDto;
import com.kgboilers.dto.centralheatingquote.TrvValveRequestDto;
import com.kgboilers.dto.centralheatingquote.InstallationItemRequestDto;
import com.kgboilers.dto.centralheatingquote.InstallationMoveDistanceRequestDto;
import com.kgboilers.dto.centralheatingquote.InstallationPipeDistanceRequestDto;
import com.kgboilers.dto.centralheatingquote.InstallationPositionRequestDto;
import com.kgboilers.dto.centralheatingquote.AddAnotherInstallationRequestDto;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.InstallationItemType;
import com.kgboilers.model.centralheatingquote.enums.InstallationPositionType;
import com.kgboilers.model.centralheatingquote.enums.MagneticFilterStatus;
import com.kgboilers.model.centralheatingquote.enums.PowerFlushStatus;
import com.kgboilers.model.centralheatingquote.enums.RadiatorConvectorType;
import com.kgboilers.model.centralheatingquote.enums.RadiatorIssueType;
import com.kgboilers.model.centralheatingquote.enums.TrvValveStatus;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.service.boilerinstallationquote.QuoteResponseFactory;
import com.kgboilers.service.boilerinstallationquote.QuoteService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteSessionService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteWizardService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Set;

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
    void setMagneticFilter_shouldRedirectToRadiatorIssues() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.MAGNETIC_FILTER)).thenReturn(true);
        when(wizardService.updateMagneticFilter(state, MagneticFilterStatus.YES_HAS)).thenReturn(CentralHeatingQuoteStep.RADIATOR_ISSUES);

        MagneticFilterRequestDto request = new MagneticFilterRequestDto();
        request.setMagneticFilterStatus(MagneticFilterStatus.YES_HAS);

        ResponseEntity<QuoteResponseDto> response = controller.setMagneticFilter(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/radiator-issues", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setRadiatorIssues_shouldRedirectToComingSoon() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_ISSUES)).thenReturn(true);
        when(wizardService.updateRadiatorIssues(
                state,
                Set.of(RadiatorIssueType.RADIATOR_LEAK, RadiatorIssueType.SOMETHING_ELSE),
                "Leak in kitchen radiator"
        )).thenReturn(CentralHeatingQuoteStep.SUMMARY);

        RadiatorIssuesRequestDto request = new RadiatorIssuesRequestDto();
        request.setRadiatorIssues(Set.of(RadiatorIssueType.RADIATOR_LEAK, RadiatorIssueType.SOMETHING_ELSE));
        request.setOtherIssueDetails("Leak in kitchen radiator");

        ResponseEntity<QuoteResponseDto> response = controller.setRadiatorIssues(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/summary", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setRadiatorIssues_shouldRedirectToInstallationItem_whenInstallRadiatorSelected() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_ISSUES)).thenReturn(true);
        when(wizardService.updateRadiatorIssues(
                state,
                Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL),
                null
        )).thenReturn(CentralHeatingQuoteStep.INSTALLATION_ITEM);

        RadiatorIssuesRequestDto request = new RadiatorIssuesRequestDto();
        request.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));

        ResponseEntity<QuoteResponseDto> response = controller.setRadiatorIssues(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/installation-item", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setRadiatorIssues_shouldRedirectToTrvInstallationQuantity_whenTrvInstallationSelected() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_ISSUES)).thenReturn(true);
        when(wizardService.updateRadiatorIssues(
                state,
                Set.of(RadiatorIssueType.INSTALL_TRV_VALVES),
                null
        )).thenReturn(CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY);

        RadiatorIssuesRequestDto request = new RadiatorIssuesRequestDto();
        request.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_TRV_VALVES));

        ResponseEntity<QuoteResponseDto> response = controller.setRadiatorIssues(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/trv-installation-quantity", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setTrvInstallationQuantity_shouldRedirectToInstallationItem() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY)).thenReturn(true);
        when(wizardService.updateTrvInstallationQuantity(state, 8, 8, 2)).thenReturn(CentralHeatingQuoteStep.INSTALLATION_ITEM);

        TrvInstallationQuantityRequestDto request = new TrvInstallationQuantityRequestDto();
        request.setTrvValvesQuantity(8);
        request.setLockshieldValvesQuantity(8);
        request.setTowelRailValvesQuantity(2);

        ResponseEntity<QuoteResponseDto> response = controller.setTrvInstallationQuantity(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/installation-item", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setInstallationItem_shouldRedirectToRadiatorSpecification() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_ITEM)).thenReturn(true);
        when(wizardService.updateInstallationItem(state, InstallationItemType.RADIATOR))
                .thenReturn(CentralHeatingQuoteStep.INSTALLATION_POSITION);

        InstallationItemRequestDto request = new InstallationItemRequestDto();
        request.setInstallationItemType(InstallationItemType.RADIATOR);

        ResponseEntity<QuoteResponseDto> response = controller.setInstallationItem(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/installation-position", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setInstallationPosition_shouldRedirectToRadiatorSpecification() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_POSITION)).thenReturn(true);
        when(wizardService.updateInstallationPosition(state, InstallationPositionType.SAME_POSITION))
                .thenReturn(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION);

        InstallationPositionRequestDto request = new InstallationPositionRequestDto();
        request.setInstallationPositionType(InstallationPositionType.SAME_POSITION);

        ResponseEntity<QuoteResponseDto> response = controller.setInstallationPosition(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/radiator-specification", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setInstallationMoveDistance_shouldRedirectToRadiatorSpecification() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE)).thenReturn(true);
        when(wizardService.updateInstallationMoveDistance(state, RelocationDistance.TWO_TO_THREE))
                .thenReturn(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION);

        InstallationMoveDistanceRequestDto request = new InstallationMoveDistanceRequestDto();
        request.setMoveDistance(RelocationDistance.TWO_TO_THREE);

        ResponseEntity<QuoteResponseDto> response = controller.setInstallationMoveDistance(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/radiator-specification", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setInstallationPipeDistance_shouldRedirectToRadiatorSpecification() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_PIPE_DISTANCE)).thenReturn(true);
        when(wizardService.updateInstallationPipeDistance(state, RelocationDistance.FOUR_TO_FIVE))
                .thenReturn(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION);

        InstallationPipeDistanceRequestDto request = new InstallationPipeDistanceRequestDto();
        request.setPipeDistance(RelocationDistance.FOUR_TO_FIVE);

        ResponseEntity<QuoteResponseDto> response = controller.setInstallationPipeDistance(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/radiator-specification", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setRadiatorSpecification_shouldRedirectToAddAnotherInstallation() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_SPECIFICATION)).thenReturn(true);
        when(wizardService.updateRadiatorSpecification(
                state,
                RadiatorConvectorType.SINGLE_CONVECTOR,
                600,
                1200,
                null,
                null,
                2
        )).thenReturn(CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION);

        RadiatorSpecificationRequestDto request = new RadiatorSpecificationRequestDto();
        request.setRadiatorConvectorType(RadiatorConvectorType.SINGLE_CONVECTOR);
        request.setRadiatorLengthMm(600);
        request.setRadiatorWidthMm(1200);
        request.setInstallationQuantity(2);

        ResponseEntity<QuoteResponseDto> response = controller.setRadiatorSpecification(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/add-another-installation", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }

    @Test
    void setAddAnotherInstallation_shouldRedirectToComingSoon_whenNo() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION)).thenReturn(true);
        when(wizardService.updateAddAnotherInstallation(state, false)).thenReturn(CentralHeatingQuoteStep.SUMMARY);

        AddAnotherInstallationRequestDto request = new AddAnotherInstallationRequestDto();
        request.setAddAnother(false);

        ResponseEntity<QuoteResponseDto> response = controller.setAddAnotherInstallation(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/central-heating-quote/summary", response.getBody().getNextStep());
        verify(sessionService).saveState(session, state);
    }
}
