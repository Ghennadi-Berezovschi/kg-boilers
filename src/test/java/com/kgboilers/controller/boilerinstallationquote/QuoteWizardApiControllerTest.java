package com.kgboilers.controller.boilerinstallationquote;

import static org.junit.jupiter.api.Assertions.assertFalse;
import com.kgboilers.dto.boilerinstallationquote.BathShowerCountRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BedroomsRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerAgeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FaultCodeDisplayRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FaultCodeDetailsRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerFloorLevelRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerLocationRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerMakeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerPressureRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerPositionRequestDto;
import com.kgboilers.dto.boilerinstallationquote.BoilerTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FlueTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FlueLengthRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FluePositionRequestDto;
import com.kgboilers.dto.boilerinstallationquote.FuelRequestDto;
import com.kgboilers.dto.boilerinstallationquote.MagneticFilterRequestDto;
import com.kgboilers.dto.boilerinstallationquote.OwnershipRequestDto;
import com.kgboilers.dto.boilerinstallationquote.PowerFlushRequestDto;
import com.kgboilers.dto.boilerinstallationquote.PropertyTypeRequestDto;
import com.kgboilers.dto.boilerinstallationquote.RepairProblemRequestDto;
import com.kgboilers.dto.boilerinstallationquote.RadiatorCountRequestDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteRequestPostcodeDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.dto.boilerinstallationquote.RelocationDistanceRequestDto;
import com.kgboilers.dto.boilerinstallationquote.RelocationRequestDto;
import com.kgboilers.dto.boilerinstallationquote.SlopedRoofPositionRequestDto;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedFuelException;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.BoilerAge;
import com.kgboilers.model.boilerinstallation.enums.BoilerFloorLevel;
import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.BoilerPressureStatus;
import com.kgboilers.model.boilerinstallation.enums.BoilerPosition;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FaultCodeDisplayStatus;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import com.kgboilers.model.boilerinstallation.enums.FluePosition;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.MagneticFilterStatus;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PowerFlushStatus;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.RepairProblem;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.boilerinstallation.enums.SlopedRoofPosition;
import com.kgboilers.model.boilerinstallation.enums.VerticalFlueType;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuoteWizardApiControllerTest {

    private QuoteService quoteService;
    private QuoteWizardService wizardService;
    private QuoteSessionService sessionService;
    private QuoteResponseFactory responseFactory;
    private HttpSession session;
    private QuoteWizardApiController controller;

    @BeforeEach
    void setUp() {
        quoteService = mock(QuoteService.class);
        wizardService = mock(QuoteWizardService.class);
        sessionService = mock(QuoteSessionService.class);
        responseFactory = new QuoteResponseFactory();
        session = mock(HttpSession.class);

        controller = new QuoteWizardApiController(
                quoteService,
                wizardService,
                sessionService,
                responseFactory
        );
    }

    @Test
    void startQuote_shouldReturnSuccessResponse_andSavePostcodeInSession() {
        when(quoteService.startQuote(anyString(), anyString())).thenReturn(QuoteStep.FUEL_TYPE);
        when(sessionService.getOrCreateState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.startWizard(any(), anyString())).thenReturn(QuoteStep.FUEL_TYPE);

        QuoteRequestPostcodeDto request = new QuoteRequestPostcodeDto();
        request.setPostcode("E16 4JJ");

        ResponseEntity<QuoteResponseDto> response = controller.startQuote(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/fuel-type", response.getBody().getNextStep());

        verify(sessionService).clearState(session);
        verify(sessionService).getOrCreateState(session);
        verify(sessionService).saveState(eq(session), any(QuoteSessionState.class));
    }

    @Test
    void setFuel_shouldReturnSessionExpired_whenStepIsNotAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.FUEL_TYPE))).thenReturn(false);

        FuelRequestDto request = new FuelRequestDto();
        request.setFuel(FuelType.GAS);

        ResponseEntity<QuoteResponseDto> response = controller.setFuel(request, session);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("SESSION_EXPIRED", response.getBody().getErrorCode());
    }

    @Test
    void setFuel_shouldReturnSuccess_whenFuelIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FUEL_TYPE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FUEL_TYPE)).thenReturn(true);
        when(wizardService.updateFuel(state, FuelType.GAS)).thenReturn(QuoteStep.PROPERTY_OWNERSHIP);

        FuelRequestDto request = new FuelRequestDto();
        request.setFuel(FuelType.GAS);

        ResponseEntity<QuoteResponseDto> response = controller.setFuel(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/property-ownership", response.getBody().getNextStep());

        verify(sessionService).saveState(session, state);
    }

    @Test
    void setFuel_shouldReturnSuccess_whenElectricFuelIsValidForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FUEL_TYPE);

        when(sessionService.getState(session)).thenReturn(state);
        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(wizardService.canAccessStep(state, QuoteStep.FUEL_TYPE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateFuel(state, FuelType.ELECTRIC, "boiler-repair"))
                .thenReturn(QuoteStep.PROPERTY_OWNERSHIP);

        FuelRequestDto request = new FuelRequestDto();
        request.setFuel(FuelType.ELECTRIC);

        ResponseEntity<QuoteResponseDto> response = controller.setFuel(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/property-ownership", response.getBody().getNextStep());

        verify(sessionService).saveState(session, state);
    }

    @Test
    void setFuel_shouldThrowException_whenFuelUnsupported() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FUEL_TYPE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FUEL_TYPE)).thenReturn(true);
        when(wizardService.updateFuel(state, FuelType.OIL))
                .thenThrow(new UnsupportedFuelException("Unsupported fuel: oil"));

        FuelRequestDto request = new FuelRequestDto();
        request.setFuel(FuelType.OIL);

        assertThrows(
                UnsupportedFuelException.class,
                () -> controller.setFuel(request, session)
        );
    }

    @Test
    void setOwnership_shouldReturnSuccess_whenOwnershipIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.PROPERTY_OWNERSHIP);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP)).thenReturn(true);
        when(wizardService.updateOwnership(state, OwnershipType.HOMEOWNER)).thenReturn(QuoteStep.PROPERTY_TYPE);

        OwnershipRequestDto request = new OwnershipRequestDto();
        request.setOwnership(OwnershipType.HOMEOWNER);

        ResponseEntity<QuoteResponseDto> response = controller.setOwnership(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/property-type", response.getBody().getNextStep());
    }

    @Test
    void setPropertyType_shouldReturnSuccess_whenPropertyTypeIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.PROPERTY_TYPE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.PROPERTY_TYPE)).thenReturn(true);
        when(wizardService.updatePropertyType(state, PropertyType.HOUSE)).thenReturn(QuoteStep.BOILER_FLOOR_LEVEL);

        PropertyTypeRequestDto request = new PropertyTypeRequestDto();
        request.setPropertyType(PropertyType.HOUSE);

        ResponseEntity<QuoteResponseDto> response = controller.setPropertyType(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/boiler-floor-level", response.getBody().getNextStep());
    }

    @Test
    void setBoilerType_shouldReturnSuccess_whenBoilerTypeIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_TYPE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_TYPE)).thenReturn(true);
        when(wizardService.updateBoilerType(state, BoilerType.COMBI)).thenReturn(QuoteStep.BOILER_POSITION);

        BoilerTypeRequestDto request = new BoilerTypeRequestDto();
        request.setBoilerType(BoilerType.COMBI);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerType(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/boiler-position", response.getBody().getNextStep());
    }

    @Test
    void setBoilerType_shouldReturnBoilerMakeForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_TYPE);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_TYPE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerType(state, BoilerType.COMBI, "boiler-repair")).thenReturn(QuoteStep.BOILER_MAKE);

        BoilerTypeRequestDto request = new BoilerTypeRequestDto();
        request.setBoilerType(BoilerType.COMBI);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerType(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/boiler-make", response.getBody().getNextStep());
    }

    @Test
    void setBoilerMake_shouldReturnBoilerLocationForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_MAKE);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_MAKE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerMake(state, BoilerMake.VAILLANT, "boiler-repair")).thenReturn(QuoteStep.BOILER_AGE);

        BoilerMakeRequestDto request = new BoilerMakeRequestDto();
        request.setBoilerMake(BoilerMake.VAILLANT);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerMake(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/boiler-age", response.getBody().getNextStep());
    }

    @Test
    void setBoilerAge_shouldReturnBoilerLocationForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_AGE);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_AGE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerAge(state, BoilerAge.TWO_TO_FIVE_YEARS, "boiler-repair")).thenReturn(QuoteStep.BOILER_LOCATION);

        BoilerAgeRequestDto request = new BoilerAgeRequestDto();
        request.setBoilerAge(BoilerAge.TWO_TO_FIVE_YEARS);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerAge(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/boiler-location", response.getBody().getNextStep());
    }

    @Test
    void setBoilerPosition_shouldReturnSuccess_whenBoilerPositionIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_POSITION);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_POSITION)).thenReturn(true);
        when(wizardService.updateBoilerPosition(state, BoilerPosition.WALL_MOUNTED)).thenReturn(QuoteStep.BOILER_LOCATION);

        BoilerPositionRequestDto request = new BoilerPositionRequestDto();
        request.setBoilerPosition(BoilerPosition.WALL_MOUNTED);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerPosition(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/boiler-location", response.getBody().getNextStep());
    }

    @Test
    void setBoilerLocation_shouldReturnSuccess_whenLocationIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_LOCATION);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_LOCATION)).thenReturn(true);
        when(wizardService.updateBoilerLocation(state, BoilerLocation.KITCHEN)).thenReturn(QuoteStep.BOILER_FLOOR_LEVEL);

        BoilerLocationRequestDto request = new BoilerLocationRequestDto();
        request.setLocation(BoilerLocation.KITCHEN);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerLocation(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/boiler-floor-level", response.getBody().getNextStep());
    }

    @Test
    void setBoilerLocation_shouldReturnRadiatorCountForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_LOCATION);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_LOCATION, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerLocation(state, BoilerLocation.KITCHEN, "boiler-repair")).thenReturn(QuoteStep.RADIATOR_COUNT);

        BoilerLocationRequestDto request = new BoilerLocationRequestDto();
        request.setLocation(BoilerLocation.KITCHEN);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerLocation(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/radiator-count", response.getBody().getNextStep());
    }

    @Test
    void setBoilerFloorLevel_shouldReturnSuccess_whenFloorLevelIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_FLOOR_LEVEL);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_FLOOR_LEVEL)).thenReturn(true);
        when(wizardService.updateBoilerFloorLevel(state, BoilerFloorLevel.BASEMENT)).thenReturn(QuoteStep.BEDROOMS);

        BoilerFloorLevelRequestDto request = new BoilerFloorLevelRequestDto();
        request.setFloorLevel(BoilerFloorLevel.BASEMENT);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerFloorLevel(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/bedrooms", response.getBody().getNextStep());
    }

    @Test
    void setBedrooms_shouldReturnSuccess_whenBedroomsAreValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BEDROOMS);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BEDROOMS)).thenReturn(true);
        when(wizardService.updateBedrooms(state, Bedrooms.THREE)).thenReturn(QuoteStep.SUMMARY);

        BedroomsRequestDto request = new BedroomsRequestDto();
        request.setBedrooms(Bedrooms.THREE);

        ResponseEntity<QuoteResponseDto> response = controller.setBedrooms(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/summary", response.getBody().getNextStep());
    }

    @Test
    void setRelocation_shouldReturnFlueType_whenRelocationIsNo() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.RELOCATION);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.RELOCATION)).thenReturn(true);
        when(wizardService.updateRelocation(state, Relocation.NO)).thenReturn(QuoteStep.FLUE_TYPE);

        RelocationRequestDto request = new RelocationRequestDto();
        request.setRelocation(Relocation.NO);

        ResponseEntity<QuoteResponseDto> response = controller.setRelocation(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/flue-type", response.getBody().getNextStep());
    }

    @Test
    void setRelocationDistance_shouldReturnFlueType_whenDistanceIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.RELOCATION_DISTANCE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.RELOCATION_DISTANCE)).thenReturn(true);
        when(wizardService.updateRelocationDistance(state, RelocationDistance.TWO_TO_THREE))
                .thenReturn(QuoteStep.FLUE_TYPE);

        RelocationDistanceRequestDto request = new RelocationDistanceRequestDto();
        request.setRelocationDistance(RelocationDistance.TWO_TO_THREE);

        ResponseEntity<QuoteResponseDto> response = controller.setRelocationDistance(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/flue-type", response.getBody().getNextStep());
    }

    @Test
    void setFlueType_shouldReturnSummary_whenFlueTypeIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FLUE_TYPE);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FLUE_TYPE)).thenReturn(true);
        when(wizardService.updateFlueType(state, FlueType.VERTICAL, VerticalFlueType.FLAT_ROOF))
                .thenReturn(QuoteStep.FLUE_LENGTH);

        FlueTypeRequestDto request = new FlueTypeRequestDto();
        request.setFlueType(FlueType.VERTICAL);
        request.setVerticalFlueType(VerticalFlueType.FLAT_ROOF);

        ResponseEntity<QuoteResponseDto> response = controller.setFlueType(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/flue-length", response.getBody().getNextStep());
    }

    @Test
    void setFlueLength_shouldReturnFluePosition_whenFlueLengthIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FLUE_LENGTH);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FLUE_LENGTH)).thenReturn(true);
        when(wizardService.updateFlueLength(state, FlueLength.TWO_TO_THREE)).thenReturn(QuoteStep.FLUE_POSITION);

        FlueLengthRequestDto request = new FlueLengthRequestDto();
        request.setFlueLength(FlueLength.TWO_TO_THREE);

        ResponseEntity<QuoteResponseDto> response = controller.setFlueLength(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/flue-position", response.getBody().getNextStep());
    }

    @Test
    void setSlopedRoofPosition_shouldReturnSuccess_whenRoofPositionIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.SLOPED_ROOF_POSITION);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.SLOPED_ROOF_POSITION)).thenReturn(true);
        when(wizardService.updateSlopedRoofPosition(state, SlopedRoofPosition.HIGHEST_TWO_THIRDS))
                .thenReturn(QuoteStep.RADIATOR_COUNT);

        SlopedRoofPositionRequestDto request = new SlopedRoofPositionRequestDto();
        request.setRoofPosition(SlopedRoofPosition.HIGHEST_TWO_THIRDS);

        ResponseEntity<QuoteResponseDto> response = controller.setSlopedRoofPosition(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/radiator-count", response.getBody().getNextStep());
    }

    @Test
    void setFluePosition_shouldReturnFlueClearance_whenFluePositionIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FLUE_POSITION);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FLUE_POSITION)).thenReturn(true);
        when(wizardService.updateFluePosition(state, FluePosition.UNDER_STRUCTURE)).thenReturn(QuoteStep.FLUE_CLEARANCE);

        FluePositionRequestDto request = new FluePositionRequestDto();
        request.setFluePosition(FluePosition.UNDER_STRUCTURE);

        ResponseEntity<QuoteResponseDto> response = controller.setFluePosition(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/flue-clearance", response.getBody().getNextStep());
    }

    @Test
    void setRadiatorCount_shouldReturnBathShowerCount_whenRadiatorCountIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.RADIATOR_COUNT);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.RADIATOR_COUNT)).thenReturn(true);
        when(wizardService.updateRadiatorCount(state, RadiatorCount.TEN_TO_THIRTEEN)).thenReturn(QuoteStep.BATH_SHOWER_COUNT);

        RadiatorCountRequestDto request = new RadiatorCountRequestDto();
        request.setRadiatorCount(RadiatorCount.TEN_TO_THIRTEEN);

        ResponseEntity<QuoteResponseDto> response = controller.setRadiatorCount(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/bath-shower-count", response.getBody().getNextStep());
    }

    @Test
    void setBathShowerCount_shouldReturnSummary_whenBathShowerCountIsValid() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BATH_SHOWER_COUNT);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BATH_SHOWER_COUNT)).thenReturn(true);
        when(wizardService.updateBathShowerCount(state, BathShowerCount.THREE)).thenReturn(QuoteStep.SUMMARY);

        BathShowerCountRequestDto request = new BathShowerCountRequestDto();
        request.setBathShowerCount(BathShowerCount.THREE);

        ResponseEntity<QuoteResponseDto> response = controller.setBathShowerCount(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/quote/summary", response.getBody().getNextStep());
    }

    @Test
    void setRepairProblem_shouldReturnBoilerPressureForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.REPAIR_PROBLEM);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.REPAIR_PROBLEM, "boiler-repair")).thenReturn(true);
        when(wizardService.updateRepairProblem(state, RepairProblem.HEATING, "boiler-repair")).thenReturn(QuoteStep.BOILER_PRESSURE);

        RepairProblemRequestDto request = new RepairProblemRequestDto();
        request.setRepairProblem(RepairProblem.HEATING);

        ResponseEntity<QuoteResponseDto> response = controller.setRepairProblem(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/boiler-pressure", response.getBody().getNextStep());
    }

    @Test
    void setPowerFlush_shouldReturnRepairProblemForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.POWER_FLUSH);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.POWER_FLUSH, "boiler-repair")).thenReturn(true);
        when(wizardService.updatePowerFlush(state, PowerFlushStatus.YES_DONE, "boiler-repair")).thenReturn(QuoteStep.MAGNETIC_FILTER);

        PowerFlushRequestDto request = new PowerFlushRequestDto();
        request.setPowerFlushStatus(PowerFlushStatus.YES_DONE);

        ResponseEntity<QuoteResponseDto> response = controller.setPowerFlush(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/magnetic-filter", response.getBody().getNextStep());
    }

    @Test
    void setMagneticFilter_shouldReturnRepairProblemForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.MAGNETIC_FILTER);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.MAGNETIC_FILTER, "boiler-repair")).thenReturn(true);
        when(wizardService.updateMagneticFilter(state, MagneticFilterStatus.YES_HAS, "boiler-repair")).thenReturn(QuoteStep.REPAIR_PROBLEM);

        MagneticFilterRequestDto request = new MagneticFilterRequestDto();
        request.setMagneticFilterStatus(MagneticFilterStatus.YES_HAS);

        ResponseEntity<QuoteResponseDto> response = controller.setMagneticFilter(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/repair-problem", response.getBody().getNextStep());
    }

    @Test
    void setBoilerPressure_shouldReturnFaultCodeStepForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.BOILER_PRESSURE);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BOILER_PRESSURE, "boiler-repair")).thenReturn(true);
        when(wizardService.updateBoilerPressure(state, BoilerPressureStatus.YES_DROPPED_OR_DROPPING, "boiler-repair")).thenReturn(QuoteStep.FAULT_CODE_DISPLAY);

        BoilerPressureRequestDto request = new BoilerPressureRequestDto();
        request.setBoilerPressureStatus(BoilerPressureStatus.YES_DROPPED_OR_DROPPING);

        ResponseEntity<QuoteResponseDto> response = controller.setBoilerPressure(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/fault-code", response.getBody().getNextStep());
    }

    @Test
    void setFaultCodeDisplay_shouldReturnDetailsStepForBoilerRepair_whenFaultCodeIsShowing() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FAULT_CODE_DISPLAY);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FAULT_CODE_DISPLAY, "boiler-repair")).thenReturn(true);
        when(wizardService.updateFaultCodeDisplay(state, FaultCodeDisplayStatus.YES_SHOWING, "boiler-repair")).thenReturn(QuoteStep.FAULT_CODE_DETAILS);

        FaultCodeDisplayRequestDto request = new FaultCodeDisplayRequestDto();
        request.setFaultCodeDisplayStatus(FaultCodeDisplayStatus.YES_SHOWING);

        ResponseEntity<QuoteResponseDto> response = controller.setFaultCodeDisplay(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/fault-code-details", response.getBody().getNextStep());
    }

    @Test
    void setFaultCodeDisplay_shouldReturnSummaryForBoilerRepair_whenNothingIsShowing() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FAULT_CODE_DISPLAY);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FAULT_CODE_DISPLAY, "boiler-repair")).thenReturn(true);
        when(wizardService.updateFaultCodeDisplay(state, FaultCodeDisplayStatus.NO_NOT_SHOWING, "boiler-repair")).thenReturn(QuoteStep.SUMMARY);

        FaultCodeDisplayRequestDto request = new FaultCodeDisplayRequestDto();
        request.setFaultCodeDisplayStatus(FaultCodeDisplayStatus.NO_NOT_SHOWING);

        ResponseEntity<QuoteResponseDto> response = controller.setFaultCodeDisplay(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/summary", response.getBody().getNextStep());
    }

    @Test
    void setFaultCodeDetails_shouldReturnSummaryForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setCurrentStep(QuoteStep.FAULT_CODE_DETAILS);

        when(session.getAttribute("service")).thenReturn("boiler-repair");
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FAULT_CODE_DETAILS, "boiler-repair")).thenReturn(true);
        when(wizardService.updateFaultCodeDetails(state, "F22 low pressure warning", "boiler-repair")).thenReturn(QuoteStep.SUMMARY);

        FaultCodeDetailsRequestDto request = new FaultCodeDetailsRequestDto();
        request.setFaultCodeDetails("F22 low pressure warning");

        ResponseEntity<QuoteResponseDto> response = controller.setFaultCodeDetails(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("/boiler-repair-quote/summary", response.getBody().getNextStep());
    }
}
