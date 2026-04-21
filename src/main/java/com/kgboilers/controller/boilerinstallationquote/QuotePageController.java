package com.kgboilers.controller.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.QuoteOfferProperties;
import com.kgboilers.dto.boilerinstallationquote.BoilerContactRequestDto;
import com.kgboilers.dto.boilerrepairquote.BoilerRepairContactRequestDto;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.BoilerAge;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.service.boilerinstallationquote.BoilerRecommendationService;
import com.kgboilers.service.boilerinstallationquote.FlueClearancePricingService;
import com.kgboilers.service.boilerinstallationquote.FlueLengthPricingService;
import com.kgboilers.service.boilerinstallationquote.FluePositionPricingService;
import com.kgboilers.service.boilerinstallationquote.QuoteLeadEmailService;
import com.kgboilers.service.boilerinstallationquote.QuoteOptionalExtraService;
import com.kgboilers.service.boilerinstallationquote.QuotePersistenceService;
import com.kgboilers.service.boilerinstallationquote.QuoteProgressService;
import com.kgboilers.service.boilerinstallationquote.RelocationPricingService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quote")
public class QuotePageController {

    private final QuoteSessionService sessionService;
    private final QuoteWizardService wizardService;
    private final RelocationPricingService relocationPricingService;
    private final FlueLengthPricingService flueLengthPricingService;
    private final FlueClearancePricingService flueClearancePricingService;
    private final FluePositionPricingService fluePositionPricingService;
    private final BoilerRecommendationService boilerRecommendationService;
    private final QuoteOptionalExtraService quoteOptionalExtraService;
    private final QuotePersistenceService quotePersistenceService;
    private final QuoteLeadEmailService quoteLeadEmailService;
    private final QuoteOfferProperties quoteOfferProperties;
    private final QuoteProgressService quoteProgressService;

    public QuotePageController(QuoteSessionService sessionService,
                               QuoteWizardService wizardService,
                               RelocationPricingService relocationPricingService,
                               FlueLengthPricingService flueLengthPricingService,
                               FlueClearancePricingService flueClearancePricingService,
                               FluePositionPricingService fluePositionPricingService,
                               BoilerRecommendationService boilerRecommendationService,
                               QuoteOptionalExtraService quoteOptionalExtraService,
                               QuotePersistenceService quotePersistenceService,
                               QuoteLeadEmailService quoteLeadEmailService,
                               QuoteOfferProperties quoteOfferProperties,
                               QuoteProgressService quoteProgressService) {
        this.sessionService = sessionService;
        this.wizardService = wizardService;
        this.relocationPricingService = relocationPricingService;
        this.flueLengthPricingService = flueLengthPricingService;
        this.flueClearancePricingService = flueClearancePricingService;
        this.fluePositionPricingService = fluePositionPricingService;
        this.boilerRecommendationService = boilerRecommendationService;
        this.quoteOptionalExtraService = quoteOptionalExtraService;
        this.quotePersistenceService = quotePersistenceService;
        this.quoteLeadEmailService = quoteLeadEmailService;
        this.quoteOfferProperties = quoteOfferProperties;
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

        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);
        boolean bookingComplete = isContactSuccess(model);
        model.addAttribute("quoteProgress", buildProgress(state, currentStep, bookingComplete, service));
    }

    @GetMapping
    public String startPage(@RequestParam(defaultValue = "boiler-installation") String service,
                            Model model,
                            HttpSession session) {

        String normalizedService = normalizeService(service);

        session.setAttribute("service", normalizedService);
        model.addAttribute("service", normalizedService);
        model.addAttribute("serviceTitle", formatServiceTitle(normalizedService));

        return "boiler-installation-quote/quote";
    }

    @GetMapping("/fuel-type")
    public String fuelTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FUEL_TYPE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.FUEL_TYPE.previous(), service));
        return "boiler-installation-quote/fuel-type";
    }

    @GetMapping("/property-ownership")
    public String ownershipPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.PROPERTY_OWNERSHIP.previous(), service));
        return "boiler-installation-quote/property-ownership";
    }

    @GetMapping("/property-type")
    public String propertyTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.PROPERTY_TYPE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.PROPERTY_TYPE.previous(), service));
        return "boiler-installation-quote/property-type";
    }

    @GetMapping("/bedrooms")
    public String bedroomsPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BEDROOMS, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BEDROOMS.previous().getPath());
        return "boiler-installation-quote/bedrooms";
    }

    @GetMapping("/boiler-type")
    public String boilerTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_TYPE, service)) {
            return redirectToStart(service);
        }

        String backUrl = "boiler-repair".equals(service)
                ? pathForService(QuoteStep.PROPERTY_TYPE, service)
                : pathForService(QuoteStep.BOILER_TYPE.previous(), service);
        model.addAttribute("backUrl", backUrl);
        return "boiler-installation-quote/boiler-type";
    }

    @GetMapping("/boiler-make")
    public String boilerMakePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !canAccessStep(state, QuoteStep.BOILER_MAKE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.BOILER_TYPE, service));
        model.addAttribute("boilerMakeOptions", BoilerMake.values());
        return "boiler-repair-quote/boiler-make";
    }

    @GetMapping("/boiler-age")
    public String boilerAgePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !canAccessStep(state, QuoteStep.BOILER_AGE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.BOILER_MAKE, service));
        model.addAttribute("boilerAgeOptions", BoilerAge.values());
        return "boiler-repair-quote/boiler-age";
    }

    @GetMapping("/boiler-conversion")
    public String boilerConversionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_CONVERSION, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BOILER_CONVERSION.previous().getPath());
        return "boiler-installation-quote/boiler-conversion";
    }


    @GetMapping("/boiler-position")
    public String boilerPositionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_POSITION, service)) {
            return "redirect:/quote";
        }

        String backUrl = QuoteStep.BOILER_TYPE.getPath();
        if (!"boiler-repair".equals(service)
                && state != null
                && state.getBoilerType() == com.kgboilers.model.boilerinstallation.enums.BoilerType.HEAT_ONLY) {
            backUrl = QuoteStep.BOILER_CONVERSION.getPath();
        }

        model.addAttribute("backUrl", backUrl);
        return "boiler-installation-quote/boiler-position";
    }


    @GetMapping("/boiler-location")
    public String boilerLocationPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_LOCATION, service)) {
            return redirectToStart(service);
        }

        String backUrl = pathForService(QuoteStep.BOILER_LOCATION.previous(), service);
        if ("boiler-repair".equals(service)) {
            backUrl = pathForService(QuoteStep.BOILER_AGE, service);
        }
        model.addAttribute("backUrl", backUrl);
        return "boiler-installation-quote/boiler-location";
    }

    @GetMapping("/boiler-floor-level")
    public String boilerFloorLevelPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if ("boiler-repair".equals(service)) {
            return canAccessStep(state, QuoteStep.RADIATOR_COUNT, service)
                    ? "redirect:" + pathForService(QuoteStep.RADIATOR_COUNT, service)
                    : redirectToStart(service);
        }

        if (!canAccessStep(state, QuoteStep.BOILER_FLOOR_LEVEL, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BOILER_LOCATION.getPath());
        return "boiler-installation-quote/boiler-floor-level";
    }

    @GetMapping("/boiler-condition")
    public String boilerConditionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_CONDITION, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BOILER_CONDITION.previous().getPath());
        return "boiler-installation-quote/boiler-condition";
    }

    @GetMapping("/relocation")
    public String relocationPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.RELOCATION, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.RELOCATION.previous().getPath());
        return "boiler-installation-quote/relocation";
    }

    @GetMapping("/relocation-distance")
    public String relocationDistancePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.RELOCATION_DISTANCE, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.RELOCATION_DISTANCE.previous().getPath());
        model.addAttribute("relocationPrices", relocationPricingService.getPricesByValue());
        return "boiler-installation-quote/relocation-distance";
    }

    @GetMapping("/flue-type")
    public String flueTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_TYPE, service)) {
            return "redirect:/quote";
        }

        String backUrl = QuoteStep.RELOCATION.getPath();
        if (state != null && state.getRelocation() == com.kgboilers.model.boilerinstallation.enums.Relocation.YES) {
            backUrl = QuoteStep.RELOCATION_DISTANCE.getPath();
        }

        model.addAttribute("backUrl", backUrl);
        return "boiler-installation-quote/flue-type";
    }

    @GetMapping("/flue-length")
    public String flueLengthPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_LENGTH, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_LENGTH.previous().getPath());
        model.addAttribute("flueLengthPrices", flueLengthPricingService.getPricesByValue());
        model.addAttribute("flueLengthImage", getFlueLengthImage(state));
        return "boiler-installation-quote/flue-length";
    }

    @GetMapping("/sloped-roof-position")
    public String slopedRoofPositionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.SLOPED_ROOF_POSITION, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.SLOPED_ROOF_POSITION.previous().getPath());
        return "boiler-installation-quote/sloped-roof-position";
    }

    @GetMapping("/flue-position")
    public String fluePositionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_POSITION, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_POSITION.previous().getPath());
        return "boiler-installation-quote/flue-position";
    }

    @GetMapping("/flue-clearance")
    public String flueClearancePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_CLEARANCE, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_CLEARANCE.previous().getPath());
        return "boiler-installation-quote/flue-clearance";
    }

    @GetMapping("/flue-property-distance")
    public String fluePropertyDistancePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_PROPERTY_DISTANCE, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_PROPERTY_DISTANCE.previous().getPath());
        return "boiler-installation-quote/flue-property-distance";
    }

    @GetMapping("/radiator-count")
    public String radiatorCountPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.RADIATOR_COUNT, service)) {
            return redirectToStart(service);
        }

        String backUrl = QuoteStep.FLUE_LENGTH.getPath();
        if ("boiler-repair".equals(service)) {
            backUrl = pathForService(QuoteStep.BOILER_LOCATION, service);
        } else if (state != null && state.getFlueType() == FlueType.HORIZONTAL) {
            backUrl = QuoteStep.FLUE_PROPERTY_DISTANCE.getPath();
        } else if (state != null
                && state.getFlueType() == FlueType.VERTICAL
                && state.getVerticalFlueType() == com.kgboilers.model.boilerinstallation.enums.VerticalFlueType.SLOPED_ROOF) {
            backUrl = QuoteStep.SLOPED_ROOF_POSITION.getPath();
        }

        model.addAttribute("backUrl", backUrl);
        return "boiler-installation-quote/radiator-count";
    }

    @GetMapping("/power-flush")
    public String powerFlushPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !canAccessStep(state, QuoteStep.POWER_FLUSH, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.RADIATOR_COUNT, service));
        return "boiler-repair-quote/power-flush";
    }

    @GetMapping("/magnetic-filter")
    public String magneticFilterPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !canAccessStep(state, QuoteStep.MAGNETIC_FILTER, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.POWER_FLUSH, service));
        return "boiler-repair-quote/magnetic-filter";
    }

    @GetMapping("/repair-problem")
    public String repairProblemPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !canAccessStep(state, QuoteStep.REPAIR_PROBLEM, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.MAGNETIC_FILTER, service));
        return "boiler-repair-quote/repair-problem";
    }

    @GetMapping("/boiler-pressure")
    public String boilerPressurePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !canAccessStep(state, QuoteStep.BOILER_PRESSURE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.REPAIR_PROBLEM, service));
        return "boiler-repair-quote/boiler-pressure";
    }

    @GetMapping("/fault-code")
    public String faultCodePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !canAccessStep(state, QuoteStep.FAULT_CODE_DISPLAY, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.BOILER_PRESSURE, service));
        return "boiler-repair-quote/fault-code";
    }

    @GetMapping("/fault-code-details")
    public String faultCodeDetailsPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !canAccessStep(state, QuoteStep.FAULT_CODE_DETAILS, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.FAULT_CODE_DISPLAY, service));
        model.addAttribute("faultCodeDetails", state != null ? state.getFaultCodeDetailsSummary() : "");
        return "boiler-repair-quote/fault-code-details";
    }

    @GetMapping("/bath-shower-count")
    public String bathShowerCountPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BATH_SHOWER_COUNT, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BATH_SHOWER_COUNT.previous().getPath());
        return "boiler-installation-quote/bath-shower-count";
    }

    @GetMapping("/summary")
    public String summaryPage(@RequestParam(value = "selectedExtras", required = false) List<String> selectedExtraIds,
                              HttpSession session,
                              Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!isComplete(state, service)) {
            return redirectToStart(service);
        }

        if ("boiler-repair".equals(service)) {
            if (!model.containsAttribute("contactRequest")) {
                model.addAttribute("contactRequest", new BoilerRepairContactRequestDto());
            }
            if (!model.containsAttribute("contactSuccess")) {
                model.addAttribute("contactSuccess", false);
            }

            populateBoilerRepairSummaryModel(model, state);
            return "boiler-repair-quote/summary";
        }

        populateSummaryModel(session, model, state, selectedExtraIds);

        return "boiler-installation-quote/summary";
    }

    @PostMapping("/repair-contact")
    public String submitRepairContactRequest(@Valid @ModelAttribute("contactRequest") BoilerRepairContactRequestDto contactRequest,
                                             BindingResult bindingResult,
                                             HttpSession session,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!"boiler-repair".equals(service) || !isComplete(state, service)) {
            return redirectToStart(service);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("contactSuccess", false);
            populateBoilerRepairSummaryModel(model, state);
            return "boiler-repair-quote/summary";
        }

        Long quoteId = quotePersistenceService.saveRepairLead(
                sessionService.getSavedQuoteId(session),
                service,
                state,
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );
        sessionService.saveSavedQuoteId(session, quoteId);

        quoteLeadEmailService.sendRepairLeadEmails(
                state,
                service,
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );

        redirectAttributes.addFlashAttribute("contactSuccess", true);
        return "redirect:" + pathForService(QuoteStep.SUMMARY, service);
    }

    @GetMapping("/contact")
    public String contactPage(@RequestParam("boiler") String boilerLabel,
                              @RequestParam(value = "selectedExtras", required = false) List<String> selectedExtraIds,
                              HttpSession session,
                              Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!isComplete(state, service)) {
            return "redirect:/quote";
        }

        SummaryViewData summaryViewData = buildSummaryViewData(session, state, selectedExtraIds);
        SelectedBoilerData selectedBoilerData = getSelectedBoilerData(summaryViewData, boilerLabel);

        if (selectedBoilerData == null) {
            return "redirect:/quote/summary";
        }

        if (!model.containsAttribute("contactRequest")) {
            BoilerContactRequestDto contactRequest = new BoilerContactRequestDto();
            contactRequest.setSelectedBoiler(selectedBoilerData.label());
            contactRequest.setSelectedExtras(summaryViewData.selectedExtraIds());
            model.addAttribute("contactRequest", contactRequest);
        }
        if (!model.containsAttribute("contactSuccess")) {
            model.addAttribute("contactSuccess", false);
        }

        populateContactPageModel(model, selectedBoilerData, summaryViewData.selectedOptionalExtras(), summaryViewData.selectedExtraIds(), summaryViewData.optionalExtrasPriceGbp());
        model.addAttribute("quoteProgress", buildProgress(state, QuoteStep.CONTACT, isContactSuccess(model), service));
        return "boiler-installation-quote/contact";
    }

    @PostMapping("/contact")
    public String submitContactRequest(@Valid @ModelAttribute("contactRequest") BoilerContactRequestDto contactRequest,
                                       BindingResult bindingResult,
                                       HttpSession session,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!isComplete(state, service)) {
            return "redirect:/quote";
        }

        SummaryViewData summaryViewData = buildSummaryViewData(session, state, contactRequest.getSelectedExtras());
        SelectedBoilerData selectedBoilerData = getSelectedBoilerData(summaryViewData, contactRequest.getSelectedBoiler());

        if (selectedBoilerData == null) {
            return "redirect:/quote/summary";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("contactSuccess", false);
            populateContactPageModel(model, selectedBoilerData, summaryViewData.selectedOptionalExtras(), summaryViewData.selectedExtraIds(), summaryViewData.optionalExtrasPriceGbp());
            model.addAttribute("quoteProgress", buildProgress(state, QuoteStep.CONTACT, false, service));
            return "boiler-installation-quote/contact";
        }

        Long quoteId = quotePersistenceService.saveLead(
                summaryViewData.quoteId(),
                normalizeService((String) session.getAttribute("service")),
                state,
                summaryViewData.boilerRecommendation(),
                summaryViewData.relocationPriceGbp(),
                summaryViewData.flueLengthPriceGbp(),
                summaryViewData.fluePositionPriceGbp(),
                summaryViewData.flueClearancePriceGbp(),
                summaryViewData.selectedOptionalExtras(),
                summaryViewData.optionalExtrasPriceGbp(),
                contactRequest.getSelectedBoiler(),
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );
        sessionService.saveSavedQuoteId(session, quoteId);
        quoteLeadEmailService.sendLeadEmails(
                state,
                normalizeService((String) session.getAttribute("service")),
                selectedBoilerData.label(),
                selectedBoilerData.totalPriceGbp(),
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getPhone(),
                summaryViewData.relocationPriceGbp(),
                summaryViewData.flueLengthPriceGbp(),
                summaryViewData.fluePositionPriceGbp(),
                summaryViewData.flueClearancePriceGbp(),
                summaryViewData.selectedOptionalExtras(),
                summaryViewData.optionalExtrasPriceGbp()
        );

        redirectAttributes.addFlashAttribute("contactSuccess", true);
        redirectAttributes.addFlashAttribute("selectedBoilerLead", contactRequest.getSelectedBoiler());

        UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/quote/contact")
                .queryParam("boiler", contactRequest.getSelectedBoiler());
        summaryViewData.selectedExtraIds().forEach(id -> redirect.queryParam("selectedExtras", id));
        return "redirect:" + redirect.toUriString();
    }

    private void populateSummaryModel(HttpSession session, Model model, QuoteSessionState state, List<String> selectedExtraIds) {
        model.addAttribute("service", getSelectedService(session));
        populateSummaryModel(model, state, buildSummaryViewData(session, state, selectedExtraIds));
    }

    private void populateBoilerRepairSummaryModel(Model model, QuoteSessionState state) {
        model.addAttribute("state", state);
        QuoteStep backStep = state != null && state.requiresFaultCodeDetails()
                ? QuoteStep.FAULT_CODE_DETAILS
                : QuoteStep.FAULT_CODE_DISPLAY;
        model.addAttribute("backUrl", pathForService(backStep, "boiler-repair"));
        model.addAttribute("requestTitle", "Boiler Repair");
        model.addAttribute("postcodeValue", defaultLine(state != null ? state.getPostcode() : null));
        model.addAttribute("fuelValue", formatSelection(state != null ? state.getFuel() : null));
        model.addAttribute("ownershipValue", formatSelection(state != null ? state.getOwnership() : null));
        model.addAttribute("propertyTypeValue", formatSelection(state != null ? state.getPropertyType() : null));
        model.addAttribute("boilerTypeValue", formatSelection(state != null ? state.getBoilerType() : null));
        model.addAttribute("boilerMakeValue", formatSelection(state != null ? state.getBoilerMake() : null));
        model.addAttribute("boilerAgeValue", defaultLine(state != null ? state.getBoilerAgeSummary() : null));
        model.addAttribute("boilerLocationValue", formatSelection(state != null ? state.getBoilerLocation() : null));
        model.addAttribute("radiatorCountValue", defaultLine(state != null ? state.getRadiatorCountSummary() : null));
        model.addAttribute("powerFlushValue", defaultLine(state != null ? state.getPowerFlushSummary() : null));
        model.addAttribute("magneticFilterValue", defaultLine(state != null ? state.getMagneticFilterSummary() : null));
        model.addAttribute("repairProblemValue", defaultLine(state != null ? state.getRepairProblemSummary() : null));
        model.addAttribute("boilerPressureValue", defaultLine(state != null ? state.getBoilerPressureSummary() : null));
        model.addAttribute("faultCodeValue", defaultLine(state != null ? state.getFaultCodeDisplaySummary() : null));
        model.addAttribute("faultCodeDetailsValue", defaultLine(state != null ? state.getFaultCodeDetailsSummary() : null));
    }

    private void populateSummaryModel(Model model, QuoteSessionState state, SummaryViewData summaryViewData) {
        String service = String.valueOf(model.asMap().getOrDefault("service", "boiler-installation"));
        model.addAttribute("state", state);

        if (state.getRelocationDistance() != null) {
            model.addAttribute("relocationPriceGbp", summaryViewData.relocationPriceGbp());
        }
        if (state.getFlueLength() != null) {
            model.addAttribute("flueLengthPriceGbp", summaryViewData.flueLengthPriceGbp());
        }
        if (state.getFluePosition() != null) {
            model.addAttribute("fluePositionPriceGbp", summaryViewData.fluePositionPriceGbp());
        }
        if (state.getFlueClearance() != null) {
            model.addAttribute("flueClearancePriceGbp", summaryViewData.flueClearancePriceGbp());
        }

        model.addAttribute("boilerRecommendation", summaryViewData.boilerRecommendation());
        model.addAttribute("backUrl", "boiler-repair".equals(service)
                ? pathForService(QuoteStep.RADIATOR_COUNT, service)
                : QuoteStep.SUMMARY.previous().getPath());
        model.addAttribute("recommendedBoilerFallbackImage", getRecommendedBoilerFallbackImage(summaryViewData.boilerRecommendation()));
        model.addAttribute("recommendedBoilerExtraPriceGbp", summaryViewData.extraPriceGbp());
        model.addAttribute("quoteOptionalExtras", quoteOptionalExtraService.getAllOptionalExtras());
        model.addAttribute("quoteIncludedItems", quoteOfferProperties.getIncludedItems());
        model.addAttribute("selectedOptionalExtras", summaryViewData.selectedOptionalExtras());
        model.addAttribute("selectedOptionalExtrasPriceGbp", summaryViewData.optionalExtrasPriceGbp());
        model.addAttribute("selectedExtraIds", summaryViewData.selectedExtraIds());
    }

    private void populateContactPageModel(Model model,
                                          SelectedBoilerData selectedBoilerData,
                                          List<QuoteOptionalExtra> selectedOptionalExtras,
                                          List<String> selectedExtraIds,
                                          int optionalExtrasPriceGbp) {
        UriComponentsBuilder backUrl = UriComponentsBuilder.fromPath("/quote/summary");
        selectedExtraIds.forEach(id -> backUrl.queryParam("selectedExtras", id));

        model.addAttribute("backUrl", backUrl.toUriString());
        model.addAttribute("selectedBoiler", selectedBoilerData.boiler());
        model.addAttribute("selectedBoilerLabel", selectedBoilerData.label());
        model.addAttribute("selectedBoilerPriceGbp", selectedBoilerData.totalPriceGbp());
        model.addAttribute("selectedBoilerImage", selectedBoilerData.image());
        model.addAttribute("selectedOptionalExtras", selectedOptionalExtras);
        model.addAttribute("selectedOptionalExtrasPriceGbp", optionalExtrasPriceGbp);
        model.addAttribute("selectedExtraIds", selectedExtraIds);
    }

    private SummaryViewData buildSummaryViewData(HttpSession session, QuoteSessionState state, List<String> selectedExtraIds) {
        int relocationPriceGbp = getRelocationPriceGbp(state);
        int flueLengthPriceGbp = getFlueLengthPriceGbp(state);
        int fluePositionPriceGbp = getFluePositionPriceGbp(state);
        int flueClearancePriceGbp = getFlueClearancePriceGbp(state);
        BoilerRecommendationResult boilerRecommendation = boilerRecommendationService.recommend(state);
        List<QuoteOptionalExtra> selectedOptionalExtras = quoteOptionalExtraService.resolveSelectedExtras(selectedExtraIds);
        int optionalExtrasPriceGbp = quoteOptionalExtraService.getTotalPriceGbp(selectedOptionalExtras);
        List<String> normalizedSelectedExtraIds = selectedOptionalExtras.stream()
                .map(QuoteOptionalExtra::getId)
                .toList();

        return new SummaryViewData(
                relocationPriceGbp,
                flueLengthPriceGbp,
                fluePositionPriceGbp,
                flueClearancePriceGbp,
                relocationPriceGbp + flueLengthPriceGbp + fluePositionPriceGbp + flueClearancePriceGbp,
                selectedOptionalExtras,
                normalizedSelectedExtraIds,
                optionalExtrasPriceGbp,
                boilerRecommendation,
                sessionService.getSavedQuoteId(session)
        );
    }

    private QuoteStep resolveCurrentStep(String requestUri) {
        return switch (requestUri) {
            case "/quote" -> QuoteStep.START;
            case "/boiler-repair-quote" -> QuoteStep.START;
            case "/quote/fuel-type" -> QuoteStep.FUEL_TYPE;
            case "/boiler-repair-quote/fuel-type" -> QuoteStep.FUEL_TYPE;
            case "/quote/property-ownership" -> QuoteStep.PROPERTY_OWNERSHIP;
            case "/boiler-repair-quote/property-ownership" -> QuoteStep.PROPERTY_OWNERSHIP;
            case "/quote/property-type" -> QuoteStep.PROPERTY_TYPE;
            case "/boiler-repair-quote/property-type" -> QuoteStep.PROPERTY_TYPE;
            case "/quote/bedrooms" -> QuoteStep.BEDROOMS;
            case "/quote/boiler-type" -> QuoteStep.BOILER_TYPE;
            case "/boiler-repair-quote/boiler-type" -> QuoteStep.BOILER_TYPE;
            case "/quote/boiler-make" -> QuoteStep.BOILER_MAKE;
            case "/boiler-repair-quote/boiler-make" -> QuoteStep.BOILER_MAKE;
            case "/quote/boiler-age" -> QuoteStep.BOILER_AGE;
            case "/boiler-repair-quote/boiler-age" -> QuoteStep.BOILER_AGE;
            case "/quote/boiler-conversion" -> QuoteStep.BOILER_CONVERSION;
            case "/quote/boiler-position" -> QuoteStep.BOILER_POSITION;
            case "/quote/boiler-location" -> QuoteStep.BOILER_LOCATION;
            case "/boiler-repair-quote/boiler-location" -> QuoteStep.BOILER_LOCATION;
            case "/quote/boiler-floor-level" -> QuoteStep.BOILER_FLOOR_LEVEL;
            case "/quote/boiler-condition" -> QuoteStep.BOILER_CONDITION;
            case "/quote/relocation" -> QuoteStep.RELOCATION;
            case "/quote/relocation-distance" -> QuoteStep.RELOCATION_DISTANCE;
            case "/quote/flue-type" -> QuoteStep.FLUE_TYPE;
            case "/quote/flue-length" -> QuoteStep.FLUE_LENGTH;
            case "/quote/sloped-roof-position" -> QuoteStep.SLOPED_ROOF_POSITION;
            case "/quote/flue-position" -> QuoteStep.FLUE_POSITION;
            case "/quote/flue-clearance" -> QuoteStep.FLUE_CLEARANCE;
            case "/quote/flue-property-distance" -> QuoteStep.FLUE_PROPERTY_DISTANCE;
            case "/quote/radiator-count" -> QuoteStep.RADIATOR_COUNT;
            case "/boiler-repair-quote/radiator-count" -> QuoteStep.RADIATOR_COUNT;
            case "/quote/power-flush" -> QuoteStep.POWER_FLUSH;
            case "/boiler-repair-quote/power-flush" -> QuoteStep.POWER_FLUSH;
            case "/quote/magnetic-filter" -> QuoteStep.MAGNETIC_FILTER;
            case "/boiler-repair-quote/magnetic-filter" -> QuoteStep.MAGNETIC_FILTER;
            case "/quote/repair-problem" -> QuoteStep.REPAIR_PROBLEM;
            case "/boiler-repair-quote/repair-problem" -> QuoteStep.REPAIR_PROBLEM;
            case "/quote/boiler-pressure" -> QuoteStep.BOILER_PRESSURE;
            case "/boiler-repair-quote/boiler-pressure" -> QuoteStep.BOILER_PRESSURE;
            case "/quote/fault-code" -> QuoteStep.FAULT_CODE_DISPLAY;
            case "/boiler-repair-quote/fault-code" -> QuoteStep.FAULT_CODE_DISPLAY;
            case "/quote/fault-code-details" -> QuoteStep.FAULT_CODE_DETAILS;
            case "/boiler-repair-quote/fault-code-details" -> QuoteStep.FAULT_CODE_DETAILS;
            case "/quote/bath-shower-count" -> QuoteStep.BATH_SHOWER_COUNT;
            case "/quote/summary" -> QuoteStep.SUMMARY;
            case "/boiler-repair-quote/summary" -> QuoteStep.SUMMARY;
            case "/quote/contact" -> QuoteStep.CONTACT;
            default -> null;
        };
    }

    private boolean isContactSuccess(Model model) {
        Map<String, Object> attributes = model.asMap();
        return attributes != null && Boolean.TRUE.equals(attributes.get("contactSuccess"));
    }

    private SelectedBoilerData getSelectedBoilerData(SummaryViewData summaryViewData, String boilerLabel) {
        BoilerRecommendationResult recommendation = summaryViewData.boilerRecommendation();
        if (recommendation == null || recommendation.getBoilers() == null || boilerLabel == null || boilerLabel.isBlank()) {
            return null;
        }

        return recommendation.getBoilers().stream()
                .filter(boiler -> boilerLabel.equals(buildBoilerLabel(boiler)))
                .findFirst()
                .map(boiler -> new SelectedBoilerData(
                        buildBoilerLabel(boiler),
                        boiler,
                        boiler.getAveragePriceGbp() + summaryViewData.extraPriceGbp() + summaryViewData.optionalExtrasPriceGbp(),
                        boiler.getImage() == null || boiler.getImage().isBlank()
                                ? getRecommendedBoilerFallbackImage(recommendation)
                                : boiler.getImage()
                ))
                .orElse(null);
    }

    private String buildBoilerLabel(com.kgboilers.model.boilerinstallationquote.BoilerModel boiler) {
        return boiler.getBrand() + " " + boiler.getModel();
    }

    private String normalizeService(String service) {
        if (service == null || service.isBlank()) {
            return "boiler-installation";
        }

        return service.trim().toLowerCase();
    }

    private String getSelectedService(HttpSession session) {
        Object service = session.getAttribute("service");
        if (service instanceof String serviceValue) {
            return normalizeService(serviceValue);
        }

        return "boiler-installation";
    }

    private String pathForService(QuoteStep step, String service) {
        if (!"boiler-repair".equals(normalizeService(service))) {
            return step.getPath();
        }

        if (step == QuoteStep.START) {
            return "/boiler-repair-quote";
        }

        return step.getPath().replaceFirst("^/quote", "/boiler-repair-quote");
    }

    private String redirectToStart(String service) {
        return "redirect:" + pathForService(QuoteStep.START, service);
    }

    private boolean canAccessStep(QuoteSessionState state, QuoteStep step, String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.canAccessStep(state, step, service);
        }

        return wizardService.canAccessStep(state, step);
    }

    private boolean isComplete(QuoteSessionState state, String service) {
        if ("boiler-repair".equals(service)) {
            return wizardService.isComplete(state, service);
        }

        return state != null && state.isComplete();
    }

    private Object buildProgress(QuoteSessionState state,
                                 QuoteStep currentStep,
                                 boolean bookingComplete,
                                 String service) {
        if ("boiler-repair".equals(service)) {
            return quoteProgressService.buildProgress(state, currentStep, bookingComplete, service);
        }

        return quoteProgressService.buildProgress(state, currentStep, bookingComplete);
    }

    private String formatServiceTitle(String service) {
        if ("central-heating".equals(service)) {
            return "Central Heating Installation & Repair";
        }

        String[] parts = service.split("-");
        StringBuilder title = new StringBuilder();

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            title.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }

        return title.toString().trim();
    }

    private String getFlueLengthImage(QuoteSessionState state) {
        if (state != null && state.getFlueType() == FlueType.HORIZONTAL) {
            return "/images/flues/flue-length-horizontal.svg";
        }

        return "/images/flues/flue-length-vertical.svg";
    }

    private String getRecommendedBoilerFallbackImage(BoilerRecommendationResult recommendation) {
        if (recommendation == null || recommendation.getTargetType() == null) {
            return "/images/boilers/combi.svg";
        }

        return switch (recommendation.getTargetType()) {
            case COMBI -> "/images/boilers/combi.svg";
            case SYSTEM -> "/images/boilers/system.svg";
            case HEAT_ONLY -> "/images/boilers/heat-only.svg";
            case OTHER -> "/images/boilers/combi.svg";
        };
    }

    private int getRelocationPriceGbp(QuoteSessionState state) {
        return state != null && state.getRelocationDistance() != null
                ? relocationPricingService.getPrice(state.getRelocationDistance())
                : 0;
    }

    private int getFlueLengthPriceGbp(QuoteSessionState state) {
        return state != null && state.getFlueLength() != null
                ? flueLengthPricingService.getPrice(state.getFlueLength())
                : 0;
    }

    private int getFlueClearancePriceGbp(QuoteSessionState state) {
        return state != null && state.getFlueClearance() != null
                ? flueClearancePricingService.getPrice(state.getFlueClearance())
                : 0;
    }

    private int getFluePositionPriceGbp(QuoteSessionState state) {
        return state != null && state.getFluePosition() != null
                ? fluePositionPricingService.getPrice(state.getFluePosition())
                : 0;
    }

    private String formatSelection(Enum<?> value) {
        if (value == null) {
            return "-";
        }

        try {
            Object raw = value.getClass().getMethod("getValue").invoke(value);
            if (raw instanceof String stringValue && !stringValue.isBlank()) {
                return humanizeValue(stringValue);
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall back to enum name.
        }

        return humanizeValue(value.name());
    }

    private String humanizeValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "-";
        }

        String normalized = rawValue.replace('_', '-').trim().toLowerCase();
        String[] parts = normalized.split("-");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            if (result.length() > 0) {
                result.append(' ');
            }

            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }

        return result.toString();
    }

    private String defaultLine(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private record SummaryViewData(int relocationPriceGbp,
                                   int flueLengthPriceGbp,
                                   int fluePositionPriceGbp,
                                   int flueClearancePriceGbp,
                                   int extraPriceGbp,
                                   List<QuoteOptionalExtra> selectedOptionalExtras,
                                   List<String> selectedExtraIds,
                                   int optionalExtrasPriceGbp,
                                   BoilerRecommendationResult boilerRecommendation,
                                   Long quoteId) {
    }

    private record SelectedBoilerData(String label,
                                      com.kgboilers.model.boilerinstallationquote.BoilerModel boiler,
                                      int totalPriceGbp,
                                      String image) {
    }
}
