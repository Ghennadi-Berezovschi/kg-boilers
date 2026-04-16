package com.kgboilers.controller.centralheatingquote;

import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteSessionService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteWizardService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CentralHeatingQuotePageControllerTest {

    private CentralHeatingQuoteSessionService sessionService;
    private CentralHeatingQuoteWizardService wizardService;
    private HttpSession session;
    private Model model;
    private CentralHeatingQuotePageController controller;

    @BeforeEach
    void setUp() {
        sessionService = mock(CentralHeatingQuoteSessionService.class);
        wizardService = mock(CentralHeatingQuoteWizardService.class);
        session = mock(HttpSession.class);
        model = mock(Model.class);

        controller = new CentralHeatingQuotePageController(sessionService, wizardService);
    }

    @Test
    void startPage_shouldRenderDedicatedCentralHeatingStartPage() {
        String view = controller.startPage(model, session);

        assertEquals("central-heating-quote/quote", view);
        verify(session).setAttribute("service", "central-heating");
        verify(model).addAttribute("service", "central-heating");
        verify(model).addAttribute("serviceTitle", "Central Heating Installation & Repair");
    }

    @Test
    void fuelTypePage_shouldReturnFuelTypePage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.FUEL_TYPE))).thenReturn(true);

        String view = controller.fuelTypePage(session, model);

        assertEquals("central-heating-quote/fuel-type", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/boiler-type");
    }

    @Test
    void radiatorCountPage_shouldReturnRadiatorCountPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.RADIATOR_COUNT))).thenReturn(true);

        String view = controller.radiatorCountPage(session, model);

        assertEquals("central-heating-quote/radiator-count", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/fuel-type");
    }

    @Test
    void trvValvesPage_shouldReturnTrvValvesPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.TRV_VALVES))).thenReturn(true);

        String view = controller.trvValvesPage(session, model);

        assertEquals("central-heating-quote/trv-valves", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/radiator-count");
    }

    @Test
    void powerFlushPage_shouldReturnPowerFlushPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.POWER_FLUSH))).thenReturn(true);

        String view = controller.powerFlushPage(session, model);

        assertEquals("central-heating-quote/power-flush", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/trv-valves");
    }

    @Test
    void magneticFilterPage_shouldReturnMagneticFilterPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.MAGNETIC_FILTER))).thenReturn(true);

        String view = controller.magneticFilterPage(session, model);

        assertEquals("central-heating-quote/magnetic-filter", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/power-flush");
    }

    @Test
    void bedroomsPage_shouldReturnBedroomsPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.BEDROOMS))).thenReturn(true);

        String view = controller.bedroomsPage(session, model);

        assertEquals("central-heating-quote/bedrooms", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/property-type");
    }

    @Test
    void boilerTypePage_shouldReturnBoilerTypePage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.BOILER_TYPE))).thenReturn(true);

        String view = controller.boilerTypePage(session, model);

        assertEquals("central-heating-quote/boiler-type", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/bedrooms");
    }
}
