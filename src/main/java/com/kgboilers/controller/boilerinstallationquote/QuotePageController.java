package com.kgboilers.controller.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.QuoteOfferProperties;
import com.kgboilers.dto.boilerinstallationquote.BoilerContactRequestDto;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.GasApplianceType;
import com.kgboilers.model.boilerinstallation.enums.GasSafetyServiceType;
import com.kgboilers.model.boilerinstallation.enums.HeatOnlyConversion;
import com.kgboilers.model.boilerinstallation.enums.HorizontalFlueShape;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.service.boilerinstallationquote.BoilerRecommendationService;
import com.kgboilers.service.boilerinstallationquote.FlueClearancePricingService;
import com.kgboilers.service.boilerinstallationquote.FlueLengthPricingService;
import com.kgboilers.service.boilerinstallationquote.FluePositionPricingService;
import com.kgboilers.service.boilerinstallationquote.QuoteLeadEmailService;
import com.kgboilers.service.boilerinstallationquote.QuoteOptionalExtraService;
import com.kgboilers.service.boilerinstallationquote.QuotePersistenceService;
import com.kgboilers.service.boilerinstallationquote.QuotePictureStorageService;
import com.kgboilers.service.boilerinstallationquote.QuoteProgressService;
import com.kgboilers.service.boilerinstallationquote.RelocationPricingService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quote")
public class QuotePageController {

    private static final String BOILER_INSTALLATION_SERVICE = "boiler-installation";
    private static final String GAS_SAFETY_CERTIFICATE_SERVICE = "gas-safety-certificate";
    private static final String HOT_WATER_CYLINDER_SERVICE = "hot-water-cylinder";
    private static final String GAS_PIPEWORK_SERVICE = "gas-pipework-and-gas-leak-detection";
    private static final String GAS_SAFETY_CERTIFICATE_LABEL = "Boiler Service and Gas Safety Certificate";
    private static final String QUOTE_SERVICE_COOKIE = "kg_quote_service";
    private static final int QUOTE_SERVICE_COOKIE_MAX_AGE_SECONDS = 2 * 60 * 60;

    private final QuoteSessionService sessionService;
    private final QuoteWizardService wizardService;
    private final RelocationPricingService relocationPricingService;
    private final FlueLengthPricingService flueLengthPricingService;
    private final FlueClearancePricingService flueClearancePricingService;
    private final FluePositionPricingService fluePositionPricingService;
    private final int squareFlueShapePriceGbp;
    private final int heatOnlyToCombiPriceGbp;
    private final BoilerRecommendationService boilerRecommendationService;
    private final QuoteOptionalExtraService quoteOptionalExtraService;
    private final QuotePersistenceService quotePersistenceService;
    private final QuotePictureStorageService quotePictureStorageService;
    private final QuoteLeadEmailService quoteLeadEmailService;
    private final QuoteOfferProperties quoteOfferProperties;
    private final QuoteProgressService quoteProgressService;

    public QuotePageController(QuoteSessionService sessionService,
                               QuoteWizardService wizardService,
                               RelocationPricingService relocationPricingService,
                               FlueLengthPricingService flueLengthPricingService,
                               FlueClearancePricingService flueClearancePricingService,
                               FluePositionPricingService fluePositionPricingService,
                               @Value("${kg.pricing.flue-shape.square:0}") int squareFlueShapePriceGbp,
                               @Value("${kg.pricing.heat-only-to-combi:0}") int heatOnlyToCombiPriceGbp,
                               BoilerRecommendationService boilerRecommendationService,
                               QuoteOptionalExtraService quoteOptionalExtraService,
                               QuotePersistenceService quotePersistenceService,
                               QuotePictureStorageService quotePictureStorageService,
                               QuoteLeadEmailService quoteLeadEmailService,
                               QuoteOfferProperties quoteOfferProperties,
                               QuoteProgressService quoteProgressService) {
        this.sessionService = sessionService;
        this.wizardService = wizardService;
        this.relocationPricingService = relocationPricingService;
        this.flueLengthPricingService = flueLengthPricingService;
        this.flueClearancePricingService = flueClearancePricingService;
        this.fluePositionPricingService = fluePositionPricingService;
        this.squareFlueShapePriceGbp = squareFlueShapePriceGbp;
        this.heatOnlyToCombiPriceGbp = heatOnlyToCombiPriceGbp;
        this.boilerRecommendationService = boilerRecommendationService;
        this.quoteOptionalExtraService = quoteOptionalExtraService;
        this.quotePersistenceService = quotePersistenceService;
        this.quotePictureStorageService = quotePictureStorageService;
        this.quoteLeadEmailService = quoteLeadEmailService;
        this.quoteOfferProperties = quoteOfferProperties;
        this.quoteProgressService = quoteProgressService;
    }

    @ModelAttribute
    public void populateServiceContext(HttpSession session, Model model) {
        model.addAttribute("service", getSelectedService(session));
        model.addAttribute("quoteApiBasePath", "/quote");
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
    public String startPage(@RequestParam(value = "service", required = false) String service,
                            Model model,
                            HttpSession session,
                            HttpServletRequest request,
                            HttpServletResponse response) {

        String normalizedService = normalizeService(service == null || service.isBlank()
                ? getSelectedService(session, request)
                : service);

        session.setAttribute("service", normalizedService);
        rememberSelectedService(response, normalizedService);

        if ((service == null || service.isBlank()) && !isDefaultInstallationService(normalizedService)) {
            return "redirect:" + pathForService(QuoteStep.START, normalizedService);
        }

        model.addAttribute("service", normalizedService);
        model.addAttribute("serviceTitle", formatServiceTitle(normalizedService));

        return "boiler-installation-quote/quote";
    }

    @GetMapping("/fuel-type")
    public String fuelTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (shouldSkipFuel(service) && canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP, service)) {
            return "redirect:/quote/property-ownership";
        }

        if (!canAccessStep(state, QuoteStep.FUEL_TYPE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.FUEL_TYPE.previous(), service));
        return "boiler-installation-quote/fuel-type";
    }

    @GetMapping("/service-type")
    public String serviceTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.SERVICE_TYPE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.START, service));
        return "boiler-installation-quote/service-type";
    }

    @GetMapping("/property-ownership")
    public String ownershipPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", getOwnershipBackUrl(state, service));
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

        if (isGasPipework(service) && isComplete(state, service)) {
            return "redirect:/quote/summary";
        }

        if (shouldSkipBedrooms(service) && canAccessStep(state, QuoteStep.BOILER_TYPE, service)) {
            return "redirect:/quote/boiler-type";
        }

        if (!canAccessStep(state, QuoteStep.BEDROOMS, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", isGasPipework(service)
                ? pathForService(QuoteStep.GAS_APPLIANCES, service)
                : pathForService(QuoteStep.BEDROOMS.previous(), service));
        return "boiler-installation-quote/bedrooms";
    }

    @GetMapping("/gas-appliances")
    public String gasAppliancesPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.GAS_APPLIANCES, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", pathForService(QuoteStep.PROPERTY_TYPE, service));
        model.addAttribute("gasApplianceOptions", GasApplianceType.values());
        return "gas-safety-certificate-quote/gas-appliances";
    }

    @GetMapping("/boiler-type")
    public String boilerTypePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_TYPE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", getBoilerTypeBackUrl(service));
        model.addAttribute("hotWaterCylinderService", isHotWaterCylinder(service));
        return "boiler-installation-quote/boiler-type";
    }

    @GetMapping("/boiler-make")
    public String boilerMakePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_MAKE, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", QuoteStep.BOILER_TYPE.getPath());
        model.addAttribute("boilerMakeOptions", BoilerMake.values());
        return "boiler-repair-quote/boiler-make";
    }

    @GetMapping("/hot-water")
    public String hotWaterPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.HOT_WATER, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", state != null && state.getBoilerType() == com.kgboilers.model.boilerinstallation.enums.BoilerType.OTHER
                ? QuoteStep.BOILER_TYPE.getPath()
                : QuoteStep.BOILER_MAKE.getPath());
        return "boiler-installation-quote/hot-water";
    }

    @GetMapping("/problem-details")
    public String problemDetailsPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.PROBLEM_DETAILS, service)) {
            return redirectToStart(service);
        }

        model.addAttribute("backUrl", QuoteStep.HOT_WATER.getPath());
        model.addAttribute("problemDetails", state != null ? state.getProblemDetailsSummary() : "");
        return "boiler-installation-quote/problem-details";
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
        if (state != null
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

        model.addAttribute("backUrl", pathForService(QuoteStep.BOILER_LOCATION.previous(), service));
        return "boiler-installation-quote/boiler-location";
    }

    @GetMapping("/boiler-floor-level")
    public String boilerFloorLevelPage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.BOILER_FLOOR_LEVEL, service)) {
            if (wizardService.shouldSkipBoilerFloorLevel(state)
                    && canAccessStep(state, QuoteStep.BOILER_CONDITION, service)) {
                return "redirect:" + pathForService(QuoteStep.BOILER_CONDITION, service);
            }
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

        String backUrl = wizardService.shouldSkipBoilerFloorLevel(state)
                ? QuoteStep.BOILER_LOCATION.getPath()
                : QuoteStep.BOILER_CONDITION.previous().getPath();
        model.addAttribute("backUrl", backUrl);
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

        String backUrl = QuoteStep.FLUE_TYPE.getPath();
        if (state != null && state.getFlueType() == FlueType.HORIZONTAL) {
            backUrl = QuoteStep.FLUE_SHAPE.getPath();
        }

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("flueLengthPrices", flueLengthPricingService.getPricesByValue());
        model.addAttribute("flueLengthImage", getFlueLengthImage(state));
        return "boiler-installation-quote/flue-length";
    }

    @GetMapping("/flue-shape")
    public String flueShapePage(HttpSession session, Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!canAccessStep(state, QuoteStep.FLUE_SHAPE, service)) {
            return "redirect:/quote";
        }

        model.addAttribute("backUrl", QuoteStep.FLUE_TYPE.getPath());
        return "boiler-installation-quote/flue-shape";
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
        if (state != null && state.getFlueType() == FlueType.HORIZONTAL) {
            backUrl = QuoteStep.FLUE_PROPERTY_DISTANCE.getPath();
        } else if (state != null
                && state.getFlueType() == FlueType.VERTICAL
                && state.getVerticalFlueType() == com.kgboilers.model.boilerinstallation.enums.VerticalFlueType.SLOPED_ROOF) {
            backUrl = QuoteStep.SLOPED_ROOF_POSITION.getPath();
        }

        model.addAttribute("backUrl", backUrl);
        return "boiler-installation-quote/radiator-count";
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

        if (isBoilerServiceAndGasSafety(service)) {
            populateServiceSummaryModel(model, state, GAS_SAFETY_CERTIFICATE_LABEL, QuoteStep.PROPERTY_TYPE.getPath());
            return "boiler-installation-quote/summary";
        }

        if (isHotWaterCylinder(service)) {
            populateServiceSummaryModel(model, state, formatServiceTitle(service), QuoteStep.PROBLEM_DETAILS.getPath());
            return "boiler-installation-quote/summary";
        }

        if (isGasPipework(service)) {
            populateServiceSummaryModel(model, state, formatServiceTitle(service), QuoteStep.PROBLEM_DETAILS.getPath());
            return "boiler-installation-quote/summary";
        }

        populateSummaryModel(session, model, state, selectedExtraIds);

        return "boiler-installation-quote/summary";
    }

    @GetMapping("/contact")
    public String contactPage(@RequestParam(value = "boiler", required = false) String boilerLabel,
                              @RequestParam(value = "selectedExtras", required = false) List<String> selectedExtraIds,
                              HttpSession session,
                              Model model) {
        QuoteSessionState state = sessionService.getState(session);
        String service = getSelectedService(session);

        if (!isComplete(state, service)) {
            return "redirect:/quote";
        }

        if (isServiceOnlyContact(service)) {
            return serviceOnlyContactPage(state, service, model);
        }

        SummaryViewData summaryViewData = buildSummaryViewData(session, state, selectedExtraIds);
        SelectedBoilerData selectedBoilerData = getSelectedBoilerData(summaryViewData, boilerLabel, service);

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

    private String serviceOnlyContactPage(QuoteSessionState state, String service, Model model) {
        String serviceLabel = formatServiceTitle(service);
        SelectedBoilerData selectedBoilerData = getServiceSelectedBoilerData(serviceLabel);

        if (!model.containsAttribute("contactRequest")) {
            BoilerContactRequestDto contactRequest = new BoilerContactRequestDto();
            contactRequest.setSelectedBoiler(selectedBoilerData.label());
            contactRequest.setSelectedExtras(List.of());
            model.addAttribute("contactRequest", contactRequest);
        }
        if (!model.containsAttribute("contactSuccess")) {
            model.addAttribute("contactSuccess", false);
        }

        populateContactPageModel(model, selectedBoilerData, List.of(), List.of(), 0);
        model.addAttribute("serviceContactSummary", true);
        model.addAttribute("serviceSummaryTitle", serviceLabel);
        model.addAttribute("state", state);
        model.addAttribute("backUrl", getServiceOnlyContactBackUrl(service));
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

        if (isServiceOnlyContact(service)) {
            contactRequest.setSelectedBoiler(formatServiceTitle(service));
            contactRequest.setSelectedExtras(List.of());
        }

        SummaryViewData summaryViewData = buildSummaryViewData(session, state, contactRequest.getSelectedExtras());
        SelectedBoilerData selectedBoilerData = getSelectedBoilerData(summaryViewData, contactRequest.getSelectedBoiler(), service);

        if (selectedBoilerData == null) {
            return "redirect:/quote/summary";
        }

        if (!bindingResult.hasErrors()) {
            try {
                state.setUploadedPictures(quotePictureStorageService.storePictures(contactRequest.getPictures()));
            } catch (IllegalArgumentException | IllegalStateException ex) {
                bindingResult.rejectValue("pictures", "pictures.invalid", ex.getMessage());
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("contactSuccess", false);
            if (isServiceOnlyContact(service)) {
                SelectedBoilerData serviceSelection = getServiceSelectedBoilerData(formatServiceTitle(service));
                populateContactPageModel(model, serviceSelection, List.of(), List.of(), 0);
                model.addAttribute("serviceContactSummary", true);
                model.addAttribute("serviceSummaryTitle", formatServiceTitle(service));
                model.addAttribute("state", state);
                model.addAttribute("backUrl", getServiceOnlyContactBackUrl(service));
                model.addAttribute("quoteProgress", buildProgress(state, QuoteStep.CONTACT, false, service));
                return "boiler-installation-quote/contact";
            }
            populateContactPageModel(model, selectedBoilerData, summaryViewData.selectedOptionalExtras(), summaryViewData.selectedExtraIds(), summaryViewData.optionalExtrasPriceGbp());
            model.addAttribute("quoteProgress", buildProgress(state, QuoteStep.CONTACT, false, service));
            return "boiler-installation-quote/contact";
        }

        Long quoteId = quotePersistenceService.saveLead(
                summaryViewData.quoteId(),
                service,
                state,
                summaryViewData.boilerRecommendation(),
                summaryViewData.relocationPriceGbp(),
                summaryViewData.flueLengthPriceGbp(),
                summaryViewData.fluePositionPriceGbp(),
                summaryViewData.flueClearancePriceGbp(),
                summaryViewData.horizontalFlueShapePriceGbp(),
                summaryViewData.heatOnlyConversionPriceGbp(),
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
                service,
                selectedBoilerData.label(),
                selectedBoilerData.totalPriceGbp(),
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getPhone(),
                summaryViewData.relocationPriceGbp(),
                summaryViewData.flueLengthPriceGbp(),
                summaryViewData.fluePositionPriceGbp(),
                summaryViewData.flueClearancePriceGbp(),
                summaryViewData.horizontalFlueShapePriceGbp(),
                summaryViewData.heatOnlyConversionPriceGbp(),
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

    private void populateServiceSummaryModel(Model model,
                                             QuoteSessionState state,
                                             String title,
                                             String backUrl) {
        model.addAttribute("serviceSummaryOnly", true);
        model.addAttribute("serviceSummaryTitle", title);
        model.addAttribute("state", state);
        model.addAttribute("backUrl", backUrl);
    }

    private void populateSummaryModel(Model model, QuoteSessionState state, SummaryViewData summaryViewData) {
        model.addAttribute("serviceSummaryOnly", false);
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
        if (summaryViewData.horizontalFlueShapePriceGbp() > 0) {
            model.addAttribute("horizontalFlueShapePriceGbp", summaryViewData.horizontalFlueShapePriceGbp());
        }
        if (summaryViewData.heatOnlyConversionPriceGbp() > 0) {
            model.addAttribute("heatOnlyConversionPriceGbp", summaryViewData.heatOnlyConversionPriceGbp());
        }

        model.addAttribute("boilerRecommendation", summaryViewData.boilerRecommendation());
        model.addAttribute("backUrl", QuoteStep.SUMMARY.previous().getPath());
        model.addAttribute("recommendedBoilerFallbackImage", getRecommendedBoilerFallbackImage(summaryViewData.boilerRecommendation()));
        model.addAttribute("recommendedBoilerExtraPriceGbp", summaryViewData.extraPriceGbp());
        model.addAttribute("quoteOptionalExtras", quoteOptionalExtraService.getOptionalExtrasFor(getOptionalExtrasBoilerType(state)));
        model.addAttribute("quoteIncludedItems", quoteOfferProperties.getIncludedItems());
        model.addAttribute("selectedOptionalExtras", summaryViewData.selectedOptionalExtras());
        model.addAttribute("selectedOptionalExtrasPriceGbp", summaryViewData.optionalExtrasPriceGbp());
        model.addAttribute("selectedExtraIds", summaryViewData.selectedExtraIds());
        model.addAttribute("selectedExtraQuantities", buildSelectedExtraQuantities(summaryViewData.selectedOptionalExtras()));
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
        int horizontalFlueShapePriceGbp = getHorizontalFlueShapePriceGbp(state);
        int heatOnlyConversionPriceGbp = getHeatOnlyConversionPriceGbp(state);
        BoilerRecommendationResult boilerRecommendation = sortBoilersForSummary(boilerRecommendationService.recommend(state));
        List<QuoteOptionalExtra> selectedOptionalExtras = quoteOptionalExtraService.resolveSelectedExtras(selectedExtraIds, getOptionalExtrasBoilerType(state));
        int optionalExtrasPriceGbp = quoteOptionalExtraService.getTotalPriceGbp(selectedOptionalExtras);
        List<String> normalizedSelectedExtraIds = selectedOptionalExtras.stream()
                .flatMap(extra -> java.util.stream.IntStream.range(0, Math.max(1, extra.getQuantity() == null ? 1 : extra.getQuantity()))
                        .mapToObj(index -> extra.getId()))
                .toList();

        return new SummaryViewData(
                relocationPriceGbp,
                flueLengthPriceGbp,
                fluePositionPriceGbp,
                flueClearancePriceGbp,
                horizontalFlueShapePriceGbp,
                heatOnlyConversionPriceGbp,
                relocationPriceGbp + flueLengthPriceGbp + fluePositionPriceGbp + flueClearancePriceGbp + horizontalFlueShapePriceGbp + heatOnlyConversionPriceGbp,
                selectedOptionalExtras,
                normalizedSelectedExtraIds,
                optionalExtrasPriceGbp,
                boilerRecommendation,
                sessionService.getSavedQuoteId(session)
        );
    }

    private Map<String, Integer> buildSelectedExtraQuantities(List<QuoteOptionalExtra> selectedOptionalExtras) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        if (selectedOptionalExtras == null) {
            return quantities;
        }

        selectedOptionalExtras.forEach(extra -> quantities.put(extra.getId(), Math.max(1, extra.getQuantity() == null ? 1 : extra.getQuantity())));
        return quantities;
    }

    private BoilerRecommendationResult sortBoilersForSummary(BoilerRecommendationResult recommendation) {
        if (recommendation == null || recommendation.getBoilers() == null || recommendation.getBoilers().size() < 2) {
            return recommendation;
        }

        List<com.kgboilers.model.boilerinstallationquote.BoilerModel> sortedBoilers = recommendation.getBoilers().stream()
                .sorted(Comparator
                        .comparingInt((com.kgboilers.model.boilerinstallationquote.BoilerModel boiler) -> boiler.getAveragePriceGbp() == null ? Integer.MAX_VALUE : boiler.getAveragePriceGbp())
                        .thenComparing(com.kgboilers.model.boilerinstallationquote.BoilerModel::getBrand, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(com.kgboilers.model.boilerinstallationquote.BoilerModel::getModel, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        if (sortedBoilers.equals(recommendation.getBoilers())) {
            return recommendation;
        }

        return new BoilerRecommendationResult(
                recommendation.getTargetType(),
                recommendation.getTargetTypeLabel(),
                recommendation.getRequiredRadiators(),
                recommendation.getRequiredBathShowerUnits(),
                recommendation.isExactMatch(),
                sortedBoilers
        );
    }

    private QuoteStep resolveCurrentStep(String requestUri) {
        return switch (requestUri) {
            case "/quote" -> QuoteStep.START;
            case "/quote/service-type" -> QuoteStep.SERVICE_TYPE;
            case "/quote/fuel-type" -> QuoteStep.FUEL_TYPE;
            case "/quote/property-ownership" -> QuoteStep.PROPERTY_OWNERSHIP;
            case "/quote/property-type" -> QuoteStep.PROPERTY_TYPE;
            case "/quote/bedrooms" -> QuoteStep.BEDROOMS;
            case "/quote/boiler-type" -> QuoteStep.BOILER_TYPE;
            case "/quote/boiler-make" -> QuoteStep.BOILER_MAKE;
            case "/quote/hot-water" -> QuoteStep.HOT_WATER;
            case "/quote/problem-details" -> QuoteStep.PROBLEM_DETAILS;
            case "/quote/gas-appliances" -> QuoteStep.GAS_APPLIANCES;
            case "/quote/boiler-conversion" -> QuoteStep.BOILER_CONVERSION;
            case "/quote/boiler-position" -> QuoteStep.BOILER_POSITION;
            case "/quote/boiler-location" -> QuoteStep.BOILER_LOCATION;
            case "/quote/boiler-floor-level" -> QuoteStep.BOILER_FLOOR_LEVEL;
            case "/quote/boiler-condition" -> QuoteStep.BOILER_CONDITION;
            case "/quote/relocation" -> QuoteStep.RELOCATION;
            case "/quote/relocation-distance" -> QuoteStep.RELOCATION_DISTANCE;
            case "/quote/flue-type" -> QuoteStep.FLUE_TYPE;
            case "/quote/flue-shape" -> QuoteStep.FLUE_SHAPE;
            case "/quote/flue-length" -> QuoteStep.FLUE_LENGTH;
            case "/quote/sloped-roof-position" -> QuoteStep.SLOPED_ROOF_POSITION;
            case "/quote/flue-position" -> QuoteStep.FLUE_POSITION;
            case "/quote/flue-clearance" -> QuoteStep.FLUE_CLEARANCE;
            case "/quote/flue-property-distance" -> QuoteStep.FLUE_PROPERTY_DISTANCE;
            case "/quote/radiator-count" -> QuoteStep.RADIATOR_COUNT;
            case "/quote/bath-shower-count" -> QuoteStep.BATH_SHOWER_COUNT;
            case "/quote/summary" -> QuoteStep.SUMMARY;
            case "/quote/contact" -> QuoteStep.CONTACT;
            default -> null;
        };
    }

    private boolean isContactSuccess(Model model) {
        Map<String, Object> attributes = model.asMap();
        return attributes != null && Boolean.TRUE.equals(attributes.get("contactSuccess"));
    }

    private SelectedBoilerData getSelectedBoilerData(SummaryViewData summaryViewData, String boilerLabel, String service) {
        if (isBoilerServiceAndGasSafety(service) && GAS_SAFETY_CERTIFICATE_LABEL.equals(boilerLabel)) {
            return getServiceSelectedBoilerData(GAS_SAFETY_CERTIFICATE_LABEL);
        }

        if (isServiceOnlyContact(service)) {
            return getServiceSelectedBoilerData(formatServiceTitle(service));
        }

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
                        summaryViewData.extraPriceGbp(),
                        summaryViewData.optionalExtrasPriceGbp(),
                        boiler.getImage() == null || boiler.getImage().isBlank()
                                ? getRecommendedBoilerFallbackImage(recommendation)
                                : boiler.getImage()
                ))
                .orElse(null);
    }

    private SelectedBoilerData getServiceSelectedBoilerData(String serviceLabel) {
        com.kgboilers.model.boilerinstallationquote.BoilerModel serviceSelection = new com.kgboilers.model.boilerinstallationquote.BoilerModel();
        serviceSelection.setBrand("Service");
        serviceSelection.setModel(serviceLabel);
        serviceSelection.setAveragePriceGbp(0);
        serviceSelection.setImage("/images/boilers/system.svg");
        return new SelectedBoilerData(
                serviceLabel,
                serviceSelection,
                0,
                0,
                serviceSelection.getImage()
        );
    }

    private String buildBoilerLabel(com.kgboilers.model.boilerinstallationquote.BoilerModel boiler) {
        return boiler.getBrand() + " " + boiler.getModel();
    }

    private String normalizeService(String service) {
        if (service == null || service.isBlank()) {
            return BOILER_INSTALLATION_SERVICE;
        }

        return service.trim().toLowerCase();
    }

    private String getSelectedService(HttpSession session) {
        Object service = session.getAttribute("service");
        if (service instanceof String serviceValue) {
            return normalizeService(serviceValue);
        }

        return BOILER_INSTALLATION_SERVICE;
    }

    private String getSelectedService(HttpSession session, HttpServletRequest request) {
        Object service = session.getAttribute("service");
        if (service instanceof String serviceValue && !serviceValue.isBlank()) {
            return normalizeService(serviceValue);
        }

        String cookieService = getSelectedServiceFromCookie(request);
        if (cookieService != null) {
            return normalizeService(cookieService);
        }

        return BOILER_INSTALLATION_SERVICE;
    }

    private String getSelectedServiceFromCookie(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (QUOTE_SERVICE_COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private void rememberSelectedService(HttpServletResponse response, String service) {
        Cookie cookie = new Cookie(QUOTE_SERVICE_COOKIE, normalizeService(service));
        cookie.setPath("/");
        cookie.setMaxAge(QUOTE_SERVICE_COOKIE_MAX_AGE_SECONDS);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private String pathForService(QuoteStep step, String service) {
        if (step == QuoteStep.START && !isDefaultInstallationService(service)) {
            return UriComponentsBuilder.fromPath(step.getPath())
                    .queryParam("service", normalizeService(service))
                    .toUriString();
        }

        return step.getPath();
    }

    private String redirectToStart(String service) {
        return "redirect:" + pathForService(QuoteStep.START, service);
    }

    private boolean canAccessStep(QuoteSessionState state, QuoteStep step, String service) {
        return isDefaultInstallationService(service)
                ? wizardService.canAccessStep(state, step)
                : wizardService.canAccessStep(state, step, service);
    }

    private boolean isComplete(QuoteSessionState state, String service) {
        return isDefaultInstallationService(service)
                ? state != null && state.isComplete()
                : wizardService.isComplete(state, service);
    }

    private Object buildProgress(QuoteSessionState state,
                                 QuoteStep currentStep,
                                 boolean bookingComplete,
                                 String service) {
        return isDefaultInstallationService(service)
                ? quoteProgressService.buildProgress(state, currentStep, bookingComplete)
                : quoteProgressService.buildProgress(state, currentStep, bookingComplete, service);
    }

    private String formatServiceTitle(String service) {
        if (isBoilerServiceAndGasSafety(service)) {
            return GAS_SAFETY_CERTIFICATE_LABEL;
        }

        if (isHotWaterCylinder(service)) {
            return "Hot Water Cylinder Installation & Repair";
        }

        if (isGasPipework(service)) {
            return "Gas Pipework And Gas Leak Detection";
        }

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

    private boolean isDefaultInstallationService(String service) {
        return BOILER_INSTALLATION_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
    }

    private boolean isBoilerServiceAndGasSafety(String service) {
        return GAS_SAFETY_CERTIFICATE_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
    }

    private String getOwnershipBackUrl(QuoteSessionState state, String service) {
        if (!isBoilerServiceAndGasSafety(service)) {
            return shouldSkipFuel(service)
                    ? pathForService(QuoteStep.START, service)
                    : pathForService(QuoteStep.PROPERTY_OWNERSHIP.previous(), service);
        }

        return requiresBoilerTypeForGasSafety(state)
                ? QuoteStep.BOILER_TYPE.getPath()
                : QuoteStep.SERVICE_TYPE.getPath();
    }

    private String getBoilerTypeBackUrl(String service) {
        if (isBoilerServiceAndGasSafety(service)) {
            return QuoteStep.SERVICE_TYPE.getPath();
        }

        return shouldSkipBedrooms(service)
                ? pathForService(QuoteStep.PROPERTY_TYPE, service)
                : pathForService(QuoteStep.BOILER_TYPE.previous(), service);
    }

    private boolean requiresBoilerTypeForGasSafety(QuoteSessionState state) {
        return state != null && requiresBoilerTypeForGasSafety(state.getGasSafetyServiceType());
    }

    private boolean requiresBoilerTypeForGasSafety(GasSafetyServiceType serviceType) {
        return serviceType == GasSafetyServiceType.BOILER_SERVICE
                || serviceType == GasSafetyServiceType.BOILER_SERVICE_AND_GAS_SAFETY_CERTIFICATE;
    }

    private boolean shouldSkipFuel(String service) {
        String normalizedService = service == null ? "" : service.trim();
        return HOT_WATER_CYLINDER_SERVICE.equalsIgnoreCase(normalizedService)
                || GAS_PIPEWORK_SERVICE.equalsIgnoreCase(normalizedService);
    }

    private boolean shouldSkipBedrooms(String service) {
        return isHotWaterCylinder(service) || isGasPipework(service);
    }

    private boolean isServiceOnlyContact(String service) {
        return isHotWaterCylinder(service) || isGasPipework(service);
    }

    private String getServiceOnlyContactBackUrl(String service) {
        return isGasPipework(service)
                ? QuoteStep.PROBLEM_DETAILS.getPath()
                : QuoteStep.PROBLEM_DETAILS.getPath();
    }

    private boolean isGasPipework(String service) {
        return GAS_PIPEWORK_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
    }

    private boolean isHotWaterCylinder(String service) {
        return HOT_WATER_CYLINDER_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
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

    private int getHeatOnlyConversionPriceGbp(QuoteSessionState state) {
        return state != null && state.getHeatOnlyConversion() == HeatOnlyConversion.YES
                ? heatOnlyToCombiPriceGbp
                : 0;
    }

    private int getHorizontalFlueShapePriceGbp(QuoteSessionState state) {
        return state != null && state.getHorizontalFlueShape() == HorizontalFlueShape.SQUARE
                ? squareFlueShapePriceGbp
                : 0;
    }

    private BoilerType getOptionalExtrasBoilerType(QuoteSessionState state) {
        if (state != null
                && state.getBoilerType() == BoilerType.HEAT_ONLY
                && state.getHeatOnlyConversion() == HeatOnlyConversion.YES) {
            return BoilerType.COMBI;
        }

        return state != null ? state.getBoilerType() : null;
    }

    private record SummaryViewData(int relocationPriceGbp,
                                   int flueLengthPriceGbp,
                                   int fluePositionPriceGbp,
                                   int flueClearancePriceGbp,
                                   int horizontalFlueShapePriceGbp,
                                   int heatOnlyConversionPriceGbp,
                                   int extraPriceGbp,
                                   List<QuoteOptionalExtra> selectedOptionalExtras,
                                   List<String> selectedExtraIds,
                                   int optionalExtrasPriceGbp,
                                   BoilerRecommendationResult boilerRecommendation,
                                   Long quoteId) {
    }

    private record SelectedBoilerData(String label,
                                      com.kgboilers.model.boilerinstallationquote.BoilerModel boiler,
                                      int installationExtrasPriceGbp,
                                      int optionalExtrasPriceGbp,
                                      String image) {
        int totalPriceGbp() {
            int catalogPriceGbp = boiler != null && boiler.getAveragePriceGbp() != null
                    ? boiler.getAveragePriceGbp()
                    : 0;
            return catalogPriceGbp + installationExtrasPriceGbp + optionalExtrasPriceGbp;
        }
    }
}
