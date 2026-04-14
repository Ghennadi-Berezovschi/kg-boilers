package com.kgboilers.controller.boilerinstallationquote;

import com.kgboilers.dto.boilerinstallationquote.BoilerContactRequestDto;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.service.boilerinstallationquote.BoilerRecommendationService;
import com.kgboilers.service.boilerinstallationquote.FlueClearancePricingService;
import com.kgboilers.service.boilerinstallationquote.FlueLengthPricingService;
import com.kgboilers.service.boilerinstallationquote.FluePositionPricingService;
import com.kgboilers.service.boilerinstallationquote.QuoteLeadEmailService;
import com.kgboilers.service.boilerinstallationquote.QuotePersistenceService;
import com.kgboilers.service.boilerinstallationquote.RelocationPricingService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final QuotePersistenceService quotePersistenceService;
    private final QuoteLeadEmailService quoteLeadEmailService;

    public QuotePageController(QuoteSessionService sessionService,
                               QuoteWizardService wizardService,
                               RelocationPricingService relocationPricingService,
                               FlueLengthPricingService flueLengthPricingService,
                               FlueClearancePricingService flueClearancePricingService,
                               FluePositionPricingService fluePositionPricingService,
                               BoilerRecommendationService boilerRecommendationService,
                               QuotePersistenceService quotePersistenceService,
                               QuoteLeadEmailService quoteLeadEmailService) {
        this.sessionService = sessionService;
        this.wizardService = wizardService;
        this.relocationPricingService = relocationPricingService;
        this.flueLengthPricingService = flueLengthPricingService;
        this.flueClearancePricingService = flueClearancePricingService;
        this.fluePositionPricingService = fluePositionPricingService;
        this.boilerRecommendationService = boilerRecommendationService;
        this.quotePersistenceService = quotePersistenceService;
        this.quoteLeadEmailService = quoteLeadEmailService;
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

        if (!wizardService.canAccessStep(state, QuoteStep.FUEL_TYPE)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FUEL_TYPE.previous().getPath());
        return "boiler-installation-quote/fuel-type";
    }

    @GetMapping("/property-ownership")
    public String ownershipPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.PROPERTY_OWNERSHIP.previous().getPath());
        return "boiler-installation-quote/property-ownership";
    }

    @GetMapping("/property-type")
    public String propertyTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.PROPERTY_TYPE)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.PROPERTY_TYPE.previous().getPath());
        return "boiler-installation-quote/property-type";
    }

    @GetMapping("/bedrooms")
    public String bedroomsPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BEDROOMS)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BEDROOMS.previous().getPath());
        return "boiler-installation-quote/bedrooms";
    }

    @GetMapping("/boiler-type")
    public String boilerTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_TYPE)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BOILER_TYPE.previous().getPath());
        return "boiler-installation-quote/boiler-type";
    }

    @GetMapping("/boiler-conversion")
    public String boilerConversionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_CONVERSION)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BOILER_CONVERSION.previous().getPath());
        return "boiler-installation-quote/boiler-conversion";
    }


    @GetMapping("/boiler-position")
    public String boilerPositionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_POSITION)) {
            return "redirect:/quote";
        }

        String backUrl = QuoteStep.BOILER_TYPE.getPath();
        if (state != null && state.getBoilerType() == com.kgboilers.model.boilerinstallation.enums.BoilerType.HEAT_ONLY) {
            backUrl = QuoteStep.BOILER_CONVERSION.getPath();
        }

        model.addAttribute("backUrl", backUrl);
        return "boiler-installation-quote/boiler-position";
    }


    @GetMapping("/boiler-location")
    public String boilerLocationPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_LOCATION)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BOILER_LOCATION.previous().getPath());
        return "boiler-installation-quote/boiler-location";
    }

    @GetMapping("/boiler-condition")
    public String boilerConditionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BOILER_CONDITION)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BOILER_CONDITION.previous().getPath());
        return "boiler-installation-quote/boiler-condition";
    }

    @GetMapping("/relocation")
    public String relocationPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.RELOCATION)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.RELOCATION.previous().getPath());
        return "boiler-installation-quote/relocation";
    }

    @GetMapping("/relocation-distance")
    public String relocationDistancePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.RELOCATION_DISTANCE)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.RELOCATION_DISTANCE.previous().getPath());
        model.addAttribute("relocationPrices", relocationPricingService.getPricesByValue());
        return "boiler-installation-quote/relocation-distance";
    }

    @GetMapping("/flue-type")
    public String flueTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_TYPE)) {
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

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_LENGTH)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_LENGTH.previous().getPath());
        model.addAttribute("flueLengthPrices", flueLengthPricingService.getPricesByValue());
        model.addAttribute("flueLengthImage", getFlueLengthImage(state));
        return "boiler-installation-quote/flue-length";
    }

    @GetMapping("/flue-position")
    public String fluePositionPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_POSITION)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_POSITION.previous().getPath());
        return "boiler-installation-quote/flue-position";
    }

    @GetMapping("/flue-clearance")
    public String flueClearancePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_CLEARANCE)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_CLEARANCE.previous().getPath());
        return "boiler-installation-quote/flue-clearance";
    }

    @GetMapping("/flue-property-distance")
    public String fluePropertyDistancePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.FLUE_PROPERTY_DISTANCE)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_PROPERTY_DISTANCE.previous().getPath());
        return "boiler-installation-quote/flue-property-distance";
    }

    @GetMapping("/radiator-count")
    public String radiatorCountPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.RADIATOR_COUNT)) {
            return "redirect:/quote";
        }

        String backUrl = QuoteStep.FLUE_LENGTH.getPath();
        if (state != null && state.getFlueType() == FlueType.HORIZONTAL) {
            backUrl = QuoteStep.FLUE_PROPERTY_DISTANCE.getPath();
        }

        model.addAttribute("backUrl", backUrl);
        return "boiler-installation-quote/radiator-count";
    }

    @GetMapping("/bath-shower-count")
    public String bathShowerCountPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (!wizardService.canAccessStep(state, QuoteStep.BATH_SHOWER_COUNT)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.BATH_SHOWER_COUNT.previous().getPath());
        return "boiler-installation-quote/bath-shower-count";
    }

    @GetMapping("/summary")
    public String summaryPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (state == null || !state.isComplete()) {
            return "redirect:/quote";
        }

        populateSummaryModel(session, model, state);

        return "boiler-installation-quote/summary";
    }

    @GetMapping("/contact")
    public String contactPage(@RequestParam("boiler") String boilerLabel,
                              HttpSession session,
                              Model model) {
        QuoteSessionState state = sessionService.getState(session);

        if (state == null || !state.isComplete()) {
            return "redirect:/quote";
        }

        SummaryViewData summaryViewData = buildSummaryViewData(session, state);
        SelectedBoilerData selectedBoilerData = getSelectedBoilerData(summaryViewData, boilerLabel);

        if (selectedBoilerData == null) {
            return "redirect:/quote/summary";
        }

        if (!model.containsAttribute("contactRequest")) {
            BoilerContactRequestDto contactRequest = new BoilerContactRequestDto();
            contactRequest.setSelectedBoiler(selectedBoilerData.label());
            model.addAttribute("contactRequest", contactRequest);
        }
        if (!model.containsAttribute("contactSuccess")) {
            model.addAttribute("contactSuccess", false);
        }

        populateContactPageModel(model, selectedBoilerData);
        return "boiler-installation-quote/contact";
    }

    @PostMapping("/contact")
    public String submitContactRequest(@Valid @ModelAttribute("contactRequest") BoilerContactRequestDto contactRequest,
                                       BindingResult bindingResult,
                                       HttpSession session,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        QuoteSessionState state = sessionService.getState(session);

        if (state == null || !state.isComplete()) {
            return "redirect:/quote";
        }

        SummaryViewData summaryViewData = buildSummaryViewData(session, state);
        SelectedBoilerData selectedBoilerData = getSelectedBoilerData(summaryViewData, contactRequest.getSelectedBoiler());

        if (selectedBoilerData == null) {
            return "redirect:/quote/summary";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("contactSuccess", false);
            populateContactPageModel(model, selectedBoilerData);
            return "boiler-installation-quote/contact";
        }

        quotePersistenceService.saveContactDetails(
                summaryViewData.quoteId(),
                contactRequest.getSelectedBoiler(),
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );
        quoteLeadEmailService.sendLeadEmails(
                state,
                selectedBoilerData.label(),
                selectedBoilerData.totalPriceGbp(),
                contactRequest.getEmail(),
                contactRequest.getPhone()
        );

        redirectAttributes.addFlashAttribute("contactSuccess", true);
        redirectAttributes.addFlashAttribute("selectedBoilerLead", contactRequest.getSelectedBoiler());
        redirectAttributes.addAttribute("boiler", contactRequest.getSelectedBoiler());

        return "redirect:/quote/contact";
    }

    private void populateSummaryModel(HttpSession session, Model model, QuoteSessionState state) {
        populateSummaryModel(model, state, buildSummaryViewData(session, state));
    }

    private void populateSummaryModel(Model model, QuoteSessionState state, SummaryViewData summaryViewData) {
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
        model.addAttribute("backUrl", QuoteStep.SUMMARY.previous().getPath());
        model.addAttribute("recommendedBoilerFallbackImage", getRecommendedBoilerFallbackImage(summaryViewData.boilerRecommendation()));
        model.addAttribute("recommendedBoilerExtraPriceGbp", summaryViewData.extraPriceGbp());
    }

    private void populateContactPageModel(Model model, SelectedBoilerData selectedBoilerData) {
        model.addAttribute("backUrl", "/quote/summary");
        model.addAttribute("selectedBoiler", selectedBoilerData.boiler());
        model.addAttribute("selectedBoilerLabel", selectedBoilerData.label());
        model.addAttribute("selectedBoilerPriceGbp", selectedBoilerData.totalPriceGbp());
        model.addAttribute("selectedBoilerImage", selectedBoilerData.image());
    }

    private SummaryViewData buildSummaryViewData(HttpSession session, QuoteSessionState state) {
        int relocationPriceGbp = getRelocationPriceGbp(state);
        int flueLengthPriceGbp = getFlueLengthPriceGbp(state);
        int fluePositionPriceGbp = getFluePositionPriceGbp(state);
        int flueClearancePriceGbp = getFlueClearancePriceGbp(state);
        BoilerRecommendationResult boilerRecommendation = boilerRecommendationService.recommend(state);
        String serviceType = normalizeService((String) session.getAttribute("service"));
        Long quoteId = quotePersistenceService.saveOrUpdate(
                sessionService.getSavedQuoteId(session),
                serviceType,
                state,
                boilerRecommendation,
                relocationPriceGbp,
                flueLengthPriceGbp,
                fluePositionPriceGbp,
                flueClearancePriceGbp
        );
        sessionService.saveSavedQuoteId(session, quoteId);

        return new SummaryViewData(
                relocationPriceGbp,
                flueLengthPriceGbp,
                fluePositionPriceGbp,
                flueClearancePriceGbp,
                relocationPriceGbp + flueLengthPriceGbp + fluePositionPriceGbp + flueClearancePriceGbp,
                boilerRecommendation,
                quoteId
        );
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
                        boiler.getAveragePriceGbp() + summaryViewData.extraPriceGbp(),
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
        return service == null ? "boiler-installation" : service.trim().toLowerCase();
    }

    private String formatServiceTitle(String service) {
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

    private record SummaryViewData(int relocationPriceGbp,
                                   int flueLengthPriceGbp,
                                   int fluePositionPriceGbp,
                                   int flueClearancePriceGbp,
                                   int extraPriceGbp,
                                   BoilerRecommendationResult boilerRecommendation,
                                   Long quoteId) {
    }

    private record SelectedBoilerData(String label,
                                      com.kgboilers.model.boilerinstallationquote.BoilerModel boiler,
                                      int totalPriceGbp,
                                      String image) {
    }
}
