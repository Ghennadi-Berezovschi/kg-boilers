package com.kgboilers.controller.centralheatingquote;

import com.kgboilers.dto.centralheatingquote.CentralHeatingContactRequestDto;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.InstallationItemType;
import com.kgboilers.model.centralheatingquote.enums.InstallationPositionType;
import com.kgboilers.model.centralheatingquote.enums.RadiatorConvectorType;
import com.kgboilers.model.centralheatingquote.enums.RadiatorIssueType;
import com.kgboilers.service.centralheatingquote.CentralHeatingLeadEmailService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuotePersistenceService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteSessionService;
import com.kgboilers.service.centralheatingquote.CentralHeatingQuoteWizardService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/central-heating-quote")
public class CentralHeatingQuotePageController {

    private final CentralHeatingQuoteSessionService sessionService;
    private final CentralHeatingQuoteWizardService wizardService;
    private final CentralHeatingQuotePersistenceService quotePersistenceService;
    private final CentralHeatingLeadEmailService leadEmailService;

    public CentralHeatingQuotePageController(CentralHeatingQuoteSessionService sessionService,
                                             CentralHeatingQuoteWizardService wizardService,
                                             CentralHeatingQuotePersistenceService quotePersistenceService,
                                             CentralHeatingLeadEmailService leadEmailService) {
        this.sessionService = sessionService;
        this.wizardService = wizardService;
        this.quotePersistenceService = quotePersistenceService;
        this.leadEmailService = leadEmailService;
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

    @GetMapping("/radiator-issues")
    public String radiatorIssuesPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_ISSUES)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.RADIATOR_ISSUES.previous().getPath());
        model.addAttribute("radiatorIssueOptions", RadiatorIssueType.values());
        return "central-heating-quote/radiator-issues";
    }

    @GetMapping("/trv-installation-quantity")
    public String trvInstallationQuantityPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", "/central-heating-quote/radiator-issues");
        model.addAttribute("existingTrvValvesQuantity", state.getTrvValvesQuantity());
        model.addAttribute("existingLockshieldValvesQuantity", state.getLockshieldValvesQuantity());
        model.addAttribute("existingTowelRailValvesQuantity", state.getTowelRailValvesQuantity());
        return "central-heating-quote/trv-installation-quantity";
    }

    @GetMapping("/installation-item")
    public String installationItemPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_ITEM)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute(
                "backUrl",
                state.needsTrvInstallationQuantity()
                        ? "/central-heating-quote/trv-installation-quantity"
                        : "/central-heating-quote/radiator-issues"
        );
        model.addAttribute("selectedInstallationItemType", state.getInstallationItemType());
        model.addAttribute("installationItemOptions", InstallationItemType.values());
        return "central-heating-quote/installation-item";
    }

    @GetMapping("/installation-position")
    public String installationPositionPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_POSITION)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.INSTALLATION_POSITION.previous().getPath());
        model.addAttribute("selectedInstallationPositionType", state.getInstallationPositionType());
        model.addAttribute("installationPositionOptions", InstallationPositionType.values());
        return "central-heating-quote/installation-position";
    }

    @GetMapping("/installation-move-distance")
    public String installationMoveDistancePage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", "/central-heating-quote/installation-position");
        return "central-heating-quote/installation-move-distance";
    }

    @GetMapping("/installation-pipe-distance")
    public String installationPipeDistancePage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_PIPE_DISTANCE)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", "/central-heating-quote/installation-position");
        return "central-heating-quote/installation-pipe-distance";
    }

    @GetMapping("/radiator-specification")
    public String radiatorSpecificationPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_SPECIFICATION)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute(
                "backUrl",
                state.getInstallationPositionType() == InstallationPositionType.DIFFERENT_POSITION
                        ? "/central-heating-quote/installation-move-distance"
                        : state.getInstallationPositionType() == InstallationPositionType.NO_EXISTING_ITEM
                        ? "/central-heating-quote/installation-pipe-distance"
                        : "/central-heating-quote/installation-position"
        );
        model.addAttribute("convectorOptions", RadiatorConvectorType.values());
        model.addAttribute("selectedInstallationItemType", state.getInstallationItemType());
        model.addAttribute("radiatorSelection", state.isRadiatorInstallation());
        model.addAttribute("towelRailSelection", state.isTowelRailInstallation());
        return "central-heating-quote/radiator-specification";
    }

    @GetMapping("/add-another-installation")
    public String addAnotherInstallationPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION)) {
            return "redirect:/central-heating-quote";
        }

        model.addAttribute("backUrl", CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION.previous().getPath());
        model.addAttribute("installationItemsPreview", state.getInstallationItemsPreview());
        return "central-heating-quote/add-another-installation";
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

    @GetMapping("/summary")
    public String summaryPage(HttpSession session, Model model) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.SUMMARY)) {
            return "redirect:/central-heating-quote";
        }

        if (!model.containsAttribute("contactRequest")) {
            model.addAttribute("contactRequest", new CentralHeatingContactRequestDto());
        }
        if (!model.containsAttribute("contactSuccess")) {
            model.addAttribute("contactSuccess", false);
        }

        populateSummaryModel(model, state);
        return "central-heating-quote/summary";
    }

    @PostMapping("/contact")
    public String submitContactRequest(@Valid @ModelAttribute("contactRequest") CentralHeatingContactRequestDto contactRequest,
                                       BindingResult bindingResult,
                                       HttpSession session,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        CentralHeatingQuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, CentralHeatingQuoteStep.SUMMARY)) {
            return "redirect:/central-heating-quote";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("contactSuccess", false);
            populateSummaryModel(model, state);
            return "central-heating-quote/summary";
        }

        Long quoteId = quotePersistenceService.saveLead(
                sessionService.getSavedQuoteId(session),
                "central-heating",
                state,
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );
        sessionService.saveSavedQuoteId(session, quoteId);

        leadEmailService.sendLeadEmails(
                state,
                "central-heating",
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );

        redirectAttributes.addFlashAttribute("contactSuccess", true);
        return "redirect:/central-heating-quote/summary";
    }

    private void populateSummaryModel(Model model, CentralHeatingQuoteSessionState state) {
        model.addAttribute(
                "backUrl",
                state.needsInstallationSpecification()
                        ? "/central-heating-quote/add-another-installation"
                        : state.needsTrvInstallationQuantity()
                        ? "/central-heating-quote/trv-installation-quantity"
                        : "/central-heating-quote/radiator-issues"
        );
        model.addAttribute("state", state);
        model.addAttribute("requestTitle", "Central Heating Installation & Repair");
        model.addAttribute("requestIssueCount", state.getRadiatorIssues() != null ? state.getRadiatorIssues().size() : 0);
        model.addAttribute("installationItemsCount", state.getInstallationItems() != null ? state.getInstallationItems().size() : 0);
        model.addAttribute("hasValveQuantities", state.hasTrvInstallationQuantity());
        model.addAttribute("hasInstallationItems", state.hasInstallationItems());
        model.addAttribute("hasOtherRadiatorIssue", state.getOtherRadiatorIssueDetails() != null && !state.getOtherRadiatorIssueDetails().isBlank());
    }
}
