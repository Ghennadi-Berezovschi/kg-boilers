package com.kgboilers.controller.centralheatingquote;

import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteSessionService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteWizardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/central-heating-quote")
public class CentralHeatingQuotePageController {

    private final CentralHeatingQuoteSessionService sessionService;
    private final CentralHeatingQuoteWizardService wizardService;

    public CentralHeatingQuotePageController(CentralHeatingQuoteSessionService sessionService,
                                             CentralHeatingQuoteWizardService wizardService) {
        this.sessionService = sessionService;
        this.wizardService = wizardService;
    }

    @GetMapping
    public String startPage(Model model, HttpSession session) {
        session.setAttribute("service", "central-heating");
        model.addAttribute("service", "central-heating");
        model.addAttribute("serviceTitle", "Central Heating Installation & Repair");
        return "central-heating-quote/quote";
    }

    @GetMapping("/fuel-type")
    public String fuelTypePage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.FUEL_TYPE)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.FUEL_TYPE.previous().getPath());
        return "central-heating-quote/fuel-type";
    }

    @GetMapping("/radiator-count")
    public String radiatorCountPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_COUNT)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.RADIATOR_COUNT.previous().getPath());
        return "central-heating-quote/radiator-count";
    }

    @GetMapping("/trv-valves")
    public String trvValvesPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.TRV_VALVES)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.TRV_VALVES.previous().getPath());
        return "central-heating-quote/trv-valves";
    }

    @GetMapping("/power-flush")
    public String powerFlushPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.POWER_FLUSH)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.POWER_FLUSH.previous().getPath());
        return "central-heating-quote/power-flush";
    }

    @GetMapping("/magnetic-filter")
    public String magneticFilterPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.MAGNETIC_FILTER)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.MAGNETIC_FILTER.previous().getPath());
        return "central-heating-quote/magnetic-filter";
    }

    @GetMapping("/property-ownership")
    public String ownershipPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.PROPERTY_OWNERSHIP)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.PROPERTY_OWNERSHIP.previous().getPath());
        return "central-heating-quote/property-ownership";
    }

    @GetMapping("/property-type")
    public String propertyTypePage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.PROPERTY_TYPE)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.PROPERTY_TYPE.previous().getPath());
        return "central-heating-quote/property-type";
    }

    @GetMapping("/boiler-type")
    public String boilerTypePage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.BOILER_TYPE)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.BOILER_TYPE.previous().getPath());
        return "central-heating-quote/boiler-type";
    }

    @GetMapping("/bedrooms")
    public String bedroomsPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.BEDROOMS)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.BEDROOMS.previous().getPath());
        return "central-heating-quote/bedrooms";
    }

    @GetMapping("/coming-soon")
    public String comingSoonPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.COMING_SOON)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.COMING_SOON.previous().getPath());
        model.addAttribute("state", state);
        return "central-heating-quote/coming-soon";
    }
}
