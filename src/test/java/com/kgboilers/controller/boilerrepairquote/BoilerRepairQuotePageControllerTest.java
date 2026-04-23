package com.kgboilers.controller.boilerrepairquote;

import com.kgboilers.dto.boilerrepairquote.BoilerRepairContactRequestDto;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.QuoteProgressView;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerrepair.enums.BoilerAge;
import com.kgboilers.model.boilerrepair.enums.FaultCodeDisplayStatus;
import com.kgboilers.service.boilerinstallationquote.QuoteLeadEmailService;
import com.kgboilers.service.boilerinstallationquote.QuotePersistenceService;
import com.kgboilers.service.boilerinstallationquote.QuoteProgressService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerrepairquote.BoilerRepairQuotePageService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BoilerRepairQuotePageControllerTest {

    private QuoteSessionService sessionService;
    private QuoteProgressService quoteProgressService;
    private QuotePersistenceService quotePersistenceService;
    private QuoteLeadEmailService quoteLeadEmailService;
    private BoilerRepairQuotePageService boilerRepairQuotePageService;
    private HttpSession session;
    private BoilerRepairQuotePageController controller;

    @BeforeEach
    void setUp() {
        sessionService = mock(QuoteSessionService.class);
        quoteProgressService = mock(QuoteProgressService.class);
        quotePersistenceService = mock(QuotePersistenceService.class);
        quoteLeadEmailService = mock(QuoteLeadEmailService.class);
        boilerRepairQuotePageService = mock(BoilerRepairQuotePageService.class);
        session = mock(HttpSession.class);

        when(quoteProgressService.buildProgress(any(), any(), anyBoolean(), eq(BoilerRepairQuotePageService.SERVICE)))
                .thenReturn(new QuoteProgressView(1, 1, 100, 0, java.util.List.of()));

        controller = new BoilerRepairQuotePageController(
                sessionService,
                quoteProgressService,
                quotePersistenceService,
                quoteLeadEmailService,
                boilerRepairQuotePageService
        );
    }

    @Test
    void startPage_shouldRenderDedicatedRepairStartPage() {
        Model model = new ExtendedModelMap();

        String view = controller.startPage(model, session);

        assertEquals("boiler-repair-quote/quote", view);
        assertEquals("boiler-repair", model.getAttribute("service"));
        assertEquals("Boiler Repair", model.getAttribute("serviceTitle"));
        verify(session).setAttribute("service", "boiler-repair");
    }

    @Test
    void boilerMakePage_shouldRenderOwnRepairPage() {
        QuoteSessionState state = new QuoteSessionState();
        Model model = new ExtendedModelMap();

        when(sessionService.getState(session)).thenReturn(state);
        when(boilerRepairQuotePageService.canAccessStep(state, QuoteStep.BOILER_MAKE)).thenReturn(true);
        when(boilerRepairQuotePageService.pathForStep(QuoteStep.BOILER_TYPE)).thenReturn("/boiler-repair-quote/boiler-type");

        String view = controller.boilerMakePage(session, model);

        assertEquals("boiler-repair-quote/boiler-make", view);
        assertEquals("/boiler-repair-quote/boiler-type", model.getAttribute("backUrl"));
        assertInstanceOf(BoilerMake[].class, model.getAttribute("boilerMakeOptions"));
    }

    @Test
    void faultCodeDetailsPage_shouldRenderOwnRepairPage() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFaultCodeDisplayStatus(FaultCodeDisplayStatus.YES_SHOWING);
        state.setFaultCodeDetails("F22 low pressure");
        Model model = new ExtendedModelMap();

        when(sessionService.getState(session)).thenReturn(state);
        when(boilerRepairQuotePageService.canAccessStep(state, QuoteStep.FAULT_CODE_DETAILS)).thenReturn(true);
        when(boilerRepairQuotePageService.pathForStep(QuoteStep.FAULT_CODE_DISPLAY)).thenReturn("/boiler-repair-quote/fault-code");

        String view = controller.faultCodeDetailsPage(session, model);

        assertEquals("boiler-repair-quote/fault-code-details", view);
        assertEquals("/boiler-repair-quote/fault-code", model.getAttribute("backUrl"));
        assertEquals("F22 low pressure", model.getAttribute("faultCodeDetails"));
    }

    @Test
    void summaryPage_shouldPopulateRepairContactForm() {
        QuoteSessionState state = completedRepairState();
        Model model = new ExtendedModelMap();

        when(sessionService.getState(session)).thenReturn(state);
        when(boilerRepairQuotePageService.isComplete(state)).thenReturn(true);

        String view = controller.summaryPage(session, model);

        assertEquals("boiler-repair-quote/summary", view);
        assertInstanceOf(BoilerRepairContactRequestDto.class, model.getAttribute("contactRequest"));
        assertEquals(false, model.getAttribute("contactSuccess"));
        verify(boilerRepairQuotePageService).populateSummaryModel(model, state);
    }

    @Test
    void submitRepairContactRequest_shouldRenderSummaryWhenValidationFails() {
        QuoteSessionState state = completedRepairState();
        Model model = new ExtendedModelMap();
        BoilerRepairContactRequestDto request = new BoilerRepairContactRequestDto();
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "contactRequest");
        bindingResult.rejectValue("email", "invalid", "Please enter a valid email address.");

        when(sessionService.getState(session)).thenReturn(state);
        when(boilerRepairQuotePageService.isComplete(state)).thenReturn(true);

        String view = controller.submitRepairContactRequest(
                request,
                bindingResult,
                session,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("boiler-repair-quote/summary", view);
        assertEquals(false, model.getAttribute("contactSuccess"));
        verify(boilerRepairQuotePageService).populateSummaryModel(model, state);
    }

    @Test
    void submitRepairContactRequest_shouldSaveLeadAndRedirectOnSuccess() {
        QuoteSessionState state = completedRepairState();
        Model model = new ExtendedModelMap();
        BoilerRepairContactRequestDto request = new BoilerRepairContactRequestDto();
        request.setName("Alex");
        request.setEmail("alex@example.com");
        request.setPhone("+447700900123");
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "contactRequest");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(sessionService.getState(session)).thenReturn(state);
        when(sessionService.getSavedQuoteId(session)).thenReturn(42L);
        when(boilerRepairQuotePageService.isComplete(state)).thenReturn(true);
        when(quotePersistenceService.saveRepairLead(42L, "boiler-repair", state, "Alex", "alex@example.com", "+447700900123"))
                .thenReturn(99L);
        when(boilerRepairQuotePageService.pathForStep(QuoteStep.SUMMARY)).thenReturn("/boiler-repair-quote/summary");

        String view = controller.submitRepairContactRequest(
                request,
                bindingResult,
                session,
                model,
                redirectAttributes
        );

        assertEquals("redirect:/boiler-repair-quote/summary", view);
        assertEquals(true, redirectAttributes.getFlashAttributes().get("contactSuccess"));
        verify(sessionService).saveSavedQuoteId(session, 99L);
        verify(quoteLeadEmailService).sendRepairLeadEmails(state, "boiler-repair", "Alex", "alex@example.com", "+447700900123");
    }

    private QuoteSessionState completedRepairState() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setBoilerType(BoilerType.COMBI);
        state.setBoilerAge(BoilerAge.TWO_TO_FIVE_YEARS);
        state.setBoilerLocation(BoilerLocation.KITCHEN);
        state.setFaultCodeDisplayStatus(FaultCodeDisplayStatus.NO_NOT_SHOWING);
        return state;
    }
}
