package com.kgboilers.controller.centralheatingquote;

import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.InstallationItemType;
import com.kgboilers.model.centralheatingquote.enums.InstallationPositionType;
import com.kgboilers.model.centralheatingquote.enums.RadiatorIssueType;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.service.centralheatingquote.CentralHeatingLeadEmailService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuotePersistenceService;
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
    private CentralHeatingQuotePersistenceService quotePersistenceService;
    private CentralHeatingLeadEmailService leadEmailService;
    private HttpSession session;
    private Model model;
    private CentralHeatingQuotePageController controller;

    @BeforeEach
    void setUp() {
        sessionService = mock(CentralHeatingQuoteSessionService.class);
        wizardService = mock(CentralHeatingQuoteWizardService.class);
        quotePersistenceService = mock(CentralHeatingQuotePersistenceService.class);
        leadEmailService = mock(CentralHeatingLeadEmailService.class);
        session = mock(HttpSession.class);
        model = mock(Model.class);

        controller = new CentralHeatingQuotePageController(sessionService, wizardService, quotePersistenceService, leadEmailService);
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
    void radiatorIssuesPage_shouldReturnRadiatorIssuesPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new CentralHeatingQuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.RADIATOR_ISSUES))).thenReturn(true);

        String view = controller.radiatorIssuesPage(session, model);

        assertEquals("central-heating-quote/radiator-issues", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/magnetic-filter");
        verify(model).addAttribute(eq("radiatorIssueOptions"), any());
    }

    @Test
    void trvInstallationQuantityPage_shouldReturnQuantityPage_whenStepAccessible() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setTrvValvesQuantity(6);
        state.setLockshieldValvesQuantity(6);
        state.setTowelRailValvesQuantity(2);
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY))).thenReturn(true);

        String view = controller.trvInstallationQuantityPage(session, model);

        assertEquals("central-heating-quote/trv-installation-quantity", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/radiator-issues");
        verify(model).addAttribute("existingTrvValvesQuantity", 6);
        verify(model).addAttribute("existingLockshieldValvesQuantity", 6);
        verify(model).addAttribute("existingTowelRailValvesQuantity", 2);
    }

    @Test
    void installationItemPage_shouldReturnInstallationItemPage_whenStepAccessible() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setInstallationItemType(InstallationItemType.RADIATOR);
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.INSTALLATION_ITEM))).thenReturn(true);

        String view = controller.installationItemPage(session, model);

        assertEquals("central-heating-quote/installation-item", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/radiator-issues");
        verify(model).addAttribute("selectedInstallationItemType", InstallationItemType.RADIATOR);
        verify(model).addAttribute(eq("installationItemOptions"), any());
    }

    @Test
    void installationItemPage_shouldUseTrvQuantityAsBackUrl_whenTrvQuantityWasRequired() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(java.util.Set.of(
                RadiatorIssueType.INSTALL_TRV_VALVES,
                RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL
        ));
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.INSTALLATION_ITEM))).thenReturn(true);

        String view = controller.installationItemPage(session, model);

        assertEquals("central-heating-quote/installation-item", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/trv-installation-quantity");
    }

    @Test
    void installationPositionPage_shouldReturnInstallationPositionPage_whenStepAccessible() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setInstallationPositionType(InstallationPositionType.SAME_POSITION);
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.INSTALLATION_POSITION))).thenReturn(true);

        String view = controller.installationPositionPage(session, model);

        assertEquals("central-heating-quote/installation-position", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/installation-item");
        verify(model).addAttribute("selectedInstallationPositionType", InstallationPositionType.SAME_POSITION);
        verify(model).addAttribute(eq("installationPositionOptions"), any());
    }

    @Test
    void installationMoveDistancePage_shouldReturnInstallationMoveDistancePage_whenStepAccessible() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setInstallationPositionType(InstallationPositionType.DIFFERENT_POSITION);
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE))).thenReturn(true);

        String view = controller.installationMoveDistancePage(session, model);

        assertEquals("central-heating-quote/installation-move-distance", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/installation-position");
    }

    @Test
    void installationPipeDistancePage_shouldReturnInstallationPipeDistancePage_whenStepAccessible() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setInstallationPositionType(InstallationPositionType.NO_EXISTING_ITEM);
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.INSTALLATION_PIPE_DISTANCE))).thenReturn(true);

        String view = controller.installationPipeDistancePage(session, model);

        assertEquals("central-heating-quote/installation-pipe-distance", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/installation-position");
    }

    @Test
    void radiatorSpecificationPage_shouldReturnRadiatorSpecificationPage_whenStepAccessible() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(java.util.Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.RADIATOR);
        state.setInstallationPositionType(InstallationPositionType.DIFFERENT_POSITION);
        state.setInstallationMoveDistance(RelocationDistance.TWO_TO_THREE);
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION))).thenReturn(true);

        String view = controller.radiatorSpecificationPage(session, model);

        assertEquals("central-heating-quote/radiator-specification", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/installation-move-distance");
        verify(model).addAttribute(eq("convectorOptions"), any());
        verify(model).addAttribute("selectedInstallationItemType", InstallationItemType.RADIATOR);
        verify(model).addAttribute("radiatorSelection", true);
        verify(model).addAttribute("towelRailSelection", false);
    }

    @Test
    void radiatorSpecificationPage_shouldUsePipeDistanceAsBackUrl_whenNoExistingItemThere() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(java.util.Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.TOWEL_RAIL);
        state.setInstallationPositionType(InstallationPositionType.NO_EXISTING_ITEM);
        state.setInstallationPipeDistance(RelocationDistance.TWO_TO_THREE);
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION))).thenReturn(true);

        String view = controller.radiatorSpecificationPage(session, model);

        assertEquals("central-heating-quote/radiator-specification", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/installation-pipe-distance");
    }

    @Test
    void addAnotherInstallationPage_shouldReturnAddAnotherInstallationPage_whenStepAccessible() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(java.util.Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.RADIATOR);
        state.setInstallationPositionType(InstallationPositionType.SAME_POSITION);
        state.setRadiatorConvectorType(com.kgboilers.model.centralheatingquote.enums.RadiatorConvectorType.SINGLE_CONVECTOR);
        state.setRadiatorLengthMm(600);
        state.setRadiatorWidthMm(1200);
        state.setInstallationQuantity(2);
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION))).thenReturn(true);

        String view = controller.addAnotherInstallationPage(session, model);

        assertEquals("central-heating-quote/add-another-installation", view);
        verify(model).addAttribute("backUrl", "/central-heating-quote/radiator-specification");
        verify(model).addAttribute(eq("installationItemsPreview"), any());
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

    @Test
    void summaryPage_shouldRenderSummary_whenStepAccessible() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(any(), eq(CentralHeatingQuoteStep.SUMMARY))).thenReturn(true);

        String view = controller.summaryPage(session, model);

        assertEquals("central-heating-quote/summary", view);
        verify(model).addAttribute("state", state);
        verify(model).addAttribute("requestTitle", "Central Heating Installation & Repair");
    }
}
