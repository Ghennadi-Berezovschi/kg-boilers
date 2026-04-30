package com.kgboilers.controller.gassafetycertificatequote;

import com.kgboilers.dto.boilerinstallationquote.BoilerContactRequestDto;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.GasApplianceType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.service.boilerinstallationquote.QuoteLeadEmailService;
import com.kgboilers.service.boilerinstallationquote.QuoteProgressService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.gassafetycertificatequote.GasSafetyCertificateQuotePageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(GasSafetyCertificateQuotePageService.BASE_PATH)
public class GasSafetyCertificateQuotePageController {

    private static final String POWER_FLUSHING_EXTRA = "Power flushing";
    private static final String MAGNETIC_FILTER_EXTRA = "Magnetic filter";

    private final QuoteSessionService sessionService;
    private final QuoteProgressService quoteProgressService;
    private final GasSafetyCertificateQuotePageService pageService;
    private final QuoteLeadEmailService quoteLeadEmailService;

    public GasSafetyCertificateQuotePageController(QuoteSessionService sessionService,
                                                   QuoteProgressService quoteProgressService,
                                                   GasSafetyCertificateQuotePageService pageService,
                                                   QuoteLeadEmailService quoteLeadEmailService) {
        this.sessionService = sessionService;
        this.quoteProgressService = quoteProgressService;
        this.pageService = pageService;
        this.quoteLeadEmailService = quoteLeadEmailService;
    }

    @ModelAttribute
    public void populateServiceContext(HttpSession session, Model model) {
        session.setAttribute("service", GasSafetyCertificateQuotePageService.SERVICE);
        model.addAttribute("service", GasSafetyCertificateQuotePageService.SERVICE);
        model.addAttribute("serviceTitle", GasSafetyCertificateQuotePageService.TITLE);
        model.addAttribute("quoteApiBasePath", GasSafetyCertificateQuotePageService.BASE_PATH);
    }

    @ModelAttribute
    public void populateQuoteProgress(HttpSession session,
                                      HttpServletRequest request,
                                      Model model) {
        QuoteStep currentStep = pageService.resolveCurrentStep(request.getRequestURI());
        if (currentStep == null) {
            return;
        }

        QuoteSessionState state = sessionService.getState(session);
        boolean contactSuccess = model.containsAttribute("contactSuccess")
                && Boolean.TRUE.equals(model.asMap().get("contactSuccess"));
        model.addAttribute(
                "quoteProgress",
                quoteProgressService.buildProgress(state, currentStep, contactSuccess, GasSafetyCertificateQuotePageService.SERVICE)
        );
    }

    @GetMapping
    public String startPage(Model model, HttpSession session) {
        session.setAttribute("service", GasSafetyCertificateQuotePageService.SERVICE);
        model.addAttribute("service", GasSafetyCertificateQuotePageService.SERVICE);
        model.addAttribute("serviceTitle", GasSafetyCertificateQuotePageService.TITLE);
        model.addAttribute("quoteApiBasePath", GasSafetyCertificateQuotePageService.BASE_PATH);
        return "gas-safety-certificate-quote/quote";
    }

    @GetMapping("/service-type")
    public String serviceTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.SERVICE_TYPE)) {
            return pageService.redirectToStart();
        }

        model.addAttribute("backUrl", pageService.pathForStep(QuoteStep.START));
        return "gas-safety-certificate-quote/service-type";
    }

    @GetMapping("/boiler-type")
    public String boilerTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.BOILER_TYPE)) {
            return pageService.redirectToStart();
        }

        model.addAttribute("backUrl", pageService.pathForStep(QuoteStep.SERVICE_TYPE));
        return "gas-safety-certificate-quote/boiler-type";
    }

    @GetMapping("/fuel-type")
    public String fuelTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.FUEL_TYPE)) {
            return pageService.redirectToStart();
        }

        model.addAttribute("backUrl", pageService.pathForStep(QuoteStep.BOILER_TYPE));
        return "gas-safety-certificate-quote/fuel-type";
    }

    @GetMapping("/boiler-make")
    public String boilerMakePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.BOILER_MAKE)) {
            return pageService.redirectToStart();
        }

        model.addAttribute("backUrl", pageService.pathForStep(QuoteStep.FUEL_TYPE));
        model.addAttribute("boilerMakeOptions", BoilerMake.values());
        return "gas-safety-certificate-quote/boiler-make";
    }

    @GetMapping("/gas-appliances")
    public String gasAppliancesPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.GAS_APPLIANCES)) {
            return pageService.redirectToStart();
        }

        model.addAttribute("backUrl", state != null && state.hasBoilerMake()
                ? pageService.pathForStep(QuoteStep.BOILER_MAKE)
                : pageService.pathForStep(QuoteStep.SERVICE_TYPE));
        model.addAttribute("gasApplianceOptions", GasApplianceType.values());
        return "gas-safety-certificate-quote/gas-appliances";
    }

    @GetMapping("/property-ownership")
    public String ownershipPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP)) {
            return pageService.redirectToStart();
        }

        model.addAttribute("backUrl", state != null && state.hasGasAppliances()
                ? pageService.pathForStep(QuoteStep.GAS_APPLIANCES)
                : pageService.pathForStep(QuoteStep.SERVICE_TYPE));
        return "gas-safety-certificate-quote/property-ownership";
    }

    @GetMapping("/property-type")
    public String propertyTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.canAccessStep(state, QuoteStep.PROPERTY_TYPE)) {
            return pageService.redirectToStart();
        }

        model.addAttribute("backUrl", pageService.pathForStep(QuoteStep.PROPERTY_OWNERSHIP));
        return "gas-safety-certificate-quote/property-type";
    }

    @GetMapping("/summary")
    public String summaryPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.isComplete(state)) {
            return pageService.redirectToStart();
        }

        model.addAttribute("serviceSummaryOnly", true);
        model.addAttribute("serviceSummaryTitle", GasSafetyCertificateQuotePageService.TITLE);
        model.addAttribute("state", state);
        model.addAttribute("backUrl", pageService.pathForStep(QuoteStep.PROPERTY_TYPE));
        if (!model.containsAttribute("contactRequest")) {
            BoilerContactRequestDto contactRequest = new BoilerContactRequestDto();
            contactRequest.setSelectedBoiler(GasSafetyCertificateQuotePageService.TITLE);
            model.addAttribute("contactRequest", contactRequest);
        }
        if (!model.containsAttribute("contactSuccess")) {
            model.addAttribute("contactSuccess", false);
        }
        if (!model.containsAttribute("selectedServiceExtras")) {
            model.addAttribute("selectedServiceExtras", java.util.List.of());
        }
        return "gas-safety-certificate-quote/summary";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute("contactRequest") BoilerContactRequestDto contactRequest,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        QuoteSessionState state = sessionService.getState(session);
        if (!pageService.isComplete(state)) {
            return pageService.redirectToStart();
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("serviceSummaryOnly", true);
            model.addAttribute("serviceSummaryTitle", GasSafetyCertificateQuotePageService.TITLE);
            model.addAttribute("state", state);
            model.addAttribute("backUrl", pageService.pathForStep(QuoteStep.PROPERTY_TYPE));
            model.addAttribute("contactSuccess", false);
            model.addAttribute("selectedServiceExtras", sanitizeServiceExtras(contactRequest.getSelectedExtras()));
            return "gas-safety-certificate-quote/summary";
        }

        java.util.List<String> selectedServiceExtras = sanitizeServiceExtras(contactRequest.getSelectedExtras());
        quoteLeadEmailService.sendServiceLeadEmails(
                state,
                GasSafetyCertificateQuotePageService.TITLE,
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getPhone(),
                selectedServiceExtras
        );

        redirectAttributes.addFlashAttribute("contactSuccess", true);
        redirectAttributes.addFlashAttribute("selectedServiceExtras", selectedServiceExtras);
        return "redirect:" + GasSafetyCertificateQuotePageService.BASE_PATH + "/summary";
    }

    private java.util.List<String> sanitizeServiceExtras(java.util.List<String> selectedExtras) {
        if (selectedExtras == null || selectedExtras.isEmpty()) {
            return java.util.List.of();
        }

        return selectedExtras.stream()
                .filter(extra -> POWER_FLUSHING_EXTRA.equals(extra) || MAGNETIC_FILTER_EXTRA.equals(extra))
                .distinct()
                .toList();
    }
}
