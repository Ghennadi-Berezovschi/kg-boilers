package com.kgboilers.controller.boilerrepairquote;

import com.kgboilers.controller.boilerinstallationquote.QuotePageController;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/boiler-repair-quote")
public class BoilerRepairQuotePageController {

    private final QuotePageController quotePageController;

    public BoilerRepairQuotePageController(QuotePageController quotePageController) {
        this.quotePageController = quotePageController;
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

    @GetMapping("/summary")
    public String summaryPage(HttpSession session, Model model) {
        session.setAttribute("service", "boiler-repair");
        return quotePageController.summaryPage(null, session, model);
    }
}
