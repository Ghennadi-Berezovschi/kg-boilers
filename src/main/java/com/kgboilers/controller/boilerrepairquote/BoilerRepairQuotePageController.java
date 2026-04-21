package com.kgboilers.controller.boilerrepairquote;

import com.kgboilers.controller.boilerinstallationquote.QuotePageController;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.service.boilerinstallationquote.QuoteProgressService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/boiler-repair-quote")
public class BoilerRepairQuotePageController {

    private final QuotePageController quotePageController;
    private final QuoteSessionService quoteSessionService;
    private final QuoteProgressService quoteProgressService;

    public BoilerRepairQuotePageController(QuotePageController quotePageController,
                                           QuoteSessionService quoteSessionService,
                                           QuoteProgressService quoteProgressService) {
        this.quotePageController = quotePageController;
        this.quoteSessionService = quoteSessionService;
        this.quoteProgressService = quoteProgressService;
    }

    @ModelAttribute
    public void populateQuoteProgress(HttpSession session,
                                      HttpServletRequest request,
                                      Model model) {
        QuoteStep currentStep = resolveCurrentStep(request.getRequestURI());
        if (currentStep == null) {
            return;
        }

        QuoteSessionState state = quoteSessionService.getState(session);
        boolean bookingComplete = Boolean.TRUE.equals(model.asMap().get("contactSuccess"));
        model.addAttribute("quoteProgress", quoteProgressService.buildProgress(state, currentStep, bookingComplete, "boiler-repair"));
    }

    @GetMapping
    public String startPage(Model model, HttpSession session) {
        session.setAttribute("service", "boiler-repair");
        model.addAttribute("service", "boiler-repair");
        model.addAttribute("serviceTitle", "Boiler Repair");
        return "boiler-repair-quote/quote";
    }

    @GetMapping("/fuel-type")
    public String fuelTypePage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.fuelTypePage(session, model);
    }

    @GetMapping("/property-ownership")
    public String ownershipPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.ownershipPage(session, model);
    }

    @GetMapping("/property-type")
    public String propertyTypePage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.propertyTypePage(session, model);
    }

    @GetMapping("/boiler-type")
    public String boilerTypePage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.boilerTypePage(session, model);
    }

    @GetMapping("/boiler-make")
    public String boilerMakePage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.boilerMakePage(session, model);
    }

    @GetMapping("/boiler-age")
    public String boilerAgePage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.boilerAgePage(session, model);
    }

    @GetMapping("/boiler-location")
    public String boilerLocationPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.boilerLocationPage(session, model);
    }

    @GetMapping("/radiator-count")
    public String radiatorCountPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.radiatorCountPage(session, model);
    }

    @GetMapping("/power-flush")
    public String powerFlushPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.powerFlushPage(session, model);
    }

    @GetMapping("/magnetic-filter")
    public String magneticFilterPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.magneticFilterPage(session, model);
    }

    @GetMapping("/repair-problem")
    public String repairProblemPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.repairProblemPage(session, model);
    }

    @GetMapping("/boiler-pressure")
    public String boilerPressurePage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.boilerPressurePage(session, model);
    }

    @GetMapping("/fault-code")
    public String faultCodePage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.faultCodePage(session, model);
    }

    @GetMapping("/fault-code-details")
    public String faultCodeDetailsPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.faultCodeDetailsPage(session, model);
    }

    @GetMapping("/summary")
    public String summaryPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.summaryPage(null, session, model);
    }

    private QuoteStep resolveCurrentStep(String requestUri) {
        return switch (requestUri) {
            case "/boiler-repair-quote" -> QuoteStep.START;
            case "/boiler-repair-quote/fuel-type" -> QuoteStep.FUEL_TYPE;
            case "/boiler-repair-quote/property-ownership" -> QuoteStep.PROPERTY_OWNERSHIP;
            case "/boiler-repair-quote/property-type" -> QuoteStep.PROPERTY_TYPE;
            case "/boiler-repair-quote/boiler-type" -> QuoteStep.BOILER_TYPE;
            case "/boiler-repair-quote/boiler-make" -> QuoteStep.BOILER_MAKE;
            case "/boiler-repair-quote/boiler-age" -> QuoteStep.BOILER_AGE;
            case "/boiler-repair-quote/boiler-location" -> QuoteStep.BOILER_LOCATION;
            case "/boiler-repair-quote/radiator-count" -> QuoteStep.RADIATOR_COUNT;
            case "/boiler-repair-quote/power-flush" -> QuoteStep.POWER_FLUSH;
            case "/boiler-repair-quote/magnetic-filter" -> QuoteStep.MAGNETIC_FILTER;
            case "/boiler-repair-quote/repair-problem" -> QuoteStep.REPAIR_PROBLEM;
            case "/boiler-repair-quote/boiler-pressure" -> QuoteStep.BOILER_PRESSURE;
            case "/boiler-repair-quote/fault-code" -> QuoteStep.FAULT_CODE_DISPLAY;
            case "/boiler-repair-quote/fault-code-details" -> QuoteStep.FAULT_CODE_DETAILS;
            case "/boiler-repair-quote/summary" -> QuoteStep.SUMMARY;
            default -> null;
        };
    }
}
