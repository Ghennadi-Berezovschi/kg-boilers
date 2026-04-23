package com.kgboilers.controller.boilerrepairquote;

import com.kgboilers.dto.boilerrepairquote.BoilerRepairContactRequestDto;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerrepair.enums.BoilerAge;
import com.kgboilers.service.boilerinstallationquote.QuoteLeadEmailService;
import com.kgboilers.service.boilerinstallationquote.QuotePersistenceService;
import com.kgboilers.service.boilerinstallationquote.QuoteProgressService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerrepairquote.BoilerRepairQuotePageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/boiler-repair-quote")
public class BoilerRepairQuotePageController {

    private final QuoteSessionService quoteSessionService;
    private final QuoteProgressService quoteProgressService;
    private final QuotePersistenceService quotePersistenceService;
    private final QuoteLeadEmailService quoteLeadEmailService;
    private final BoilerRepairQuotePageService boilerRepairQuotePageService;

    public BoilerRepairQuotePageController(QuoteSessionService quoteSessionService,
                                           QuoteProgressService quoteProgressService,
                                           QuotePersistenceService quotePersistenceService,
                                           QuoteLeadEmailService quoteLeadEmailService,
                                           BoilerRepairQuotePageService boilerRepairQuotePageService) {
        this.quoteSessionService = quoteSessionService;
        this.quoteProgressService = quoteProgressService;
        this.quotePersistenceService = quotePersistenceService;
        this.quoteLeadEmailService = quoteLeadEmailService;
        this.boilerRepairQuotePageService = boilerRepairQuotePageService;
    }

    @ModelAttribute
    public void populateServiceContext(HttpSession session, Model model) {
        session.setAttribute("service", BoilerRepairQuotePageService.SERVICE);
        model.addAttribute("service", BoilerRepairQuotePageService.SERVICE);
        model.addAttribute("serviceTitle", "Boiler Repair");
        model.addAttribute("quoteApiBasePath", "/boiler-repair-quote");
    }

    @ModelAttribute
    public void populateQuoteProgress(HttpSession session,
                                      HttpServletRequest request,
                                      Model model) {
        QuoteStep currentStep = boilerRepairQuotePageService.resolveCurrentStep(request.getRequestURI());
        if (currentStep == null) {
            return;
        }

        QuoteSessionState state = quoteSessionService.getState(session);
        boolean bookingComplete = Boolean.TRUE.equals(model.asMap().get("contactSuccess"));
        model.addAttribute("quoteProgress", quoteProgressService.buildProgress(state, currentStep, bookingComplete, BoilerRepairQuotePageService.SERVICE));
    }

    @GetMapping
    public String startPage(Model model, HttpSession session) {
        session.setAttribute("service", BoilerRepairQuotePageService.SERVICE);
        model.addAttribute("service", BoilerRepairQuotePageService.SERVICE);
        model.addAttribute("serviceTitle", "Boiler Repair");
        return "boiler-repair-quote/quote";
    }

    @GetMapping("/fuel-type")
    public String fuelTypePage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.FUEL_TYPE)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.START));
        return "boiler-installation-quote/fuel-type";
    }

    @GetMapping("/property-ownership")
    public String ownershipPage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.FUEL_TYPE));
        return "boiler-installation-quote/property-ownership";
    }

    @GetMapping("/property-type")
    public String propertyTypePage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.PROPERTY_TYPE)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.PROPERTY_OWNERSHIP));
        return "boiler-installation-quote/property-type";
    }

    @GetMapping("/boiler-type")
    public String boilerTypePage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.BOILER_TYPE)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.PROPERTY_TYPE));
        return "boiler-installation-quote/boiler-type";
    }

    @GetMapping("/boiler-make")
    public String boilerMakePage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.BOILER_MAKE)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.BOILER_TYPE));
        model.addAttribute("boilerMakeOptions", BoilerMake.values());
        return "boiler-repair-quote/boiler-make";
    }

    @GetMapping("/boiler-age")
    public String boilerAgePage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.BOILER_AGE)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.BOILER_MAKE));
        model.addAttribute("boilerAgeOptions", BoilerAge.values());
        return "boiler-repair-quote/boiler-age";
    }

    @GetMapping("/boiler-location")
    public String boilerLocationPage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.BOILER_LOCATION)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.BOILER_AGE));
        return "boiler-installation-quote/boiler-location";
    }

    @GetMapping("/radiator-count")
    public String radiatorCountPage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.RADIATOR_COUNT)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.BOILER_LOCATION));
        return "boiler-installation-quote/radiator-count";
    }

    @GetMapping("/power-flush")
    public String powerFlushPage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.POWER_FLUSH)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.RADIATOR_COUNT));
        return "boiler-repair-quote/power-flush";
    }

    @GetMapping("/magnetic-filter")
    public String magneticFilterPage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.MAGNETIC_FILTER)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.POWER_FLUSH));
        return "boiler-repair-quote/magnetic-filter";
    }

    @GetMapping("/repair-problem")
    public String repairProblemPage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.REPAIR_PROBLEM)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.MAGNETIC_FILTER));
        return "boiler-repair-quote/repair-problem";
    }

    @GetMapping("/boiler-pressure")
    public String boilerPressurePage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.BOILER_PRESSURE)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.REPAIR_PROBLEM));
        return "boiler-repair-quote/boiler-pressure";
    }

    @GetMapping("/fault-code")
    public String faultCodePage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.FAULT_CODE_DISPLAY)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.BOILER_PRESSURE));
        return "boiler-repair-quote/fault-code";
    }

    @GetMapping("/fault-code-details")
    public String faultCodeDetailsPage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.canAccessStep(state, QuoteStep.FAULT_CODE_DETAILS)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        model.addAttribute("backUrl", boilerRepairQuotePageService.pathForStep(QuoteStep.FAULT_CODE_DISPLAY));
        model.addAttribute("faultCodeDetails", state != null ? state.getFaultCodeDetailsSummary() : "");
        return "boiler-repair-quote/fault-code-details";
    }

    @GetMapping("/summary")
    public String summaryPage(HttpSession session, Model model) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.isComplete(state)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        if (!model.containsAttribute("contactRequest")) {
            model.addAttribute("contactRequest", new BoilerRepairContactRequestDto());
        }
        if (!model.containsAttribute("contactSuccess")) {
            model.addAttribute("contactSuccess", false);
        }

        boilerRepairQuotePageService.populateSummaryModel(model, state);
        return "boiler-repair-quote/summary";
    }

    @PostMapping("/repair-contact")
    public String submitRepairContactRequest(@Valid @ModelAttribute("contactRequest") BoilerRepairContactRequestDto contactRequest,
                                             BindingResult bindingResult,
                                             HttpSession session,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {
        QuoteSessionState state = quoteSessionService.getState(session);
        if (!boilerRepairQuotePageService.isComplete(state)) {
            return boilerRepairQuotePageService.redirectToStart();
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("contactSuccess", false);
            boilerRepairQuotePageService.populateSummaryModel(model, state);
            return "boiler-repair-quote/summary";
        }

        Long quoteId = quotePersistenceService.saveRepairLead(
                quoteSessionService.getSavedQuoteId(session),
                BoilerRepairQuotePageService.SERVICE,
                state,
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );
        quoteSessionService.saveSavedQuoteId(session, quoteId);

        quoteLeadEmailService.sendRepairLeadEmails(
                state,
                BoilerRepairQuotePageService.SERVICE,
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );

        redirectAttributes.addFlashAttribute("contactSuccess", true);
        return "redirect:" + boilerRepairQuotePageService.pathForStep(QuoteStep.SUMMARY);
    }
}
