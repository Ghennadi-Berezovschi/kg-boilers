package com.kgboilers.controller.boilerinstallationquote;

import com.kgboilers.dto.boilerinstallationquote.BoilerContactRequestDto;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BoilerCondition;
import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import com.kgboilers.model.boilerinstallation.enums.BoilerPosition;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueClearance;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import com.kgboilers.model.boilerinstallation.enums.FluePosition;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.boilerinstallation.enums.VerticalFlueType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class QuotePageControllerTest {

    private QuoteSessionService sessionService;
    private QuoteWizardService wizardService;
    private RelocationPricingService relocationPricingService;
    private FlueLengthPricingService flueLengthPricingService;
    private FlueClearancePricingService flueClearancePricingService;
    private FluePositionPricingService fluePositionPricingService;
    private BoilerRecommendationService boilerRecommendationService;
    private QuotePersistenceService quotePersistenceService;
    private QuoteLeadEmailService quoteLeadEmailService;
    private HttpSession session;
    private Model model;
    private QuotePageController controller;

    @BeforeEach
    void setUp() {
        sessionService = mock(QuoteSessionService.class);
        wizardService = mock(QuoteWizardService.class);
        relocationPricingService = mock(RelocationPricingService.class);
        flueLengthPricingService = mock(FlueLengthPricingService.class);
        flueClearancePricingService = mock(FlueClearancePricingService.class);
        fluePositionPricingService = mock(FluePositionPricingService.class);
        boilerRecommendationService = mock(BoilerRecommendationService.class);
        quotePersistenceService = mock(QuotePersistenceService.class);
        quoteLeadEmailService = mock(QuoteLeadEmailService.class);
        session = mock(HttpSession.class);
        model = mock(Model.class);

        when(relocationPricingService.getPricesByValue()).thenReturn(Map.of());
        when(flueLengthPricingService.getPricesByValue()).thenReturn(Map.of());

        controller = new QuotePageController(
                sessionService,
                wizardService,
                relocationPricingService,
                flueLengthPricingService,
                flueClearancePricingService,
                fluePositionPricingService,
                boilerRecommendationService,
                quotePersistenceService,
                quoteLeadEmailService
        );
    }

    @Test
    void startPage_shouldReturnQuotePage_andSaveServiceInSessionAndModel() {
        String view = controller.startPage("Boiler-Installation", model, session);

        assertEquals("boiler-installation-quote/quote", view);
        verify(session).setAttribute(eq("service"), eq("boiler-installation"));
        verify(model).addAttribute(eq("service"), eq("boiler-installation"));
        verify(model).addAttribute(eq("serviceTitle"), eq("Boiler Installation"));
    }

    @Test
    void fuelTypePage_shouldRedirectToQuote_whenStepNotAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.FUEL_TYPE))).thenReturn(false);

        String view = controller.fuelTypePage(session, model);

        assertEquals("redirect:/quote", view);
    }

    @Test
    void fuelTypePage_shouldReturnFuelTypePage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.FUEL_TYPE))).thenReturn(true);

        String view = controller.fuelTypePage(session, model);

        assertEquals("boiler-installation-quote/fuel-type", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote"));
    }

    @Test
    void ownershipPage_shouldReturnOwnershipPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.PROPERTY_OWNERSHIP))).thenReturn(true);

        String view = controller.ownershipPage(session, model);

        assertEquals("boiler-installation-quote/property-ownership", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/fuel-type"));
    }

    @Test
    void propertyTypePage_shouldReturnPropertyTypePage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.PROPERTY_TYPE))).thenReturn(true);

        String view = controller.propertyTypePage(session, model);

        assertEquals("boiler-installation-quote/property-type", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/property-ownership"));
    }

    @Test
    void boilerTypePage_shouldReturnBoilerTypePage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.BOILER_TYPE))).thenReturn(true);

        String view = controller.boilerTypePage(session, model);

        assertEquals("boiler-installation-quote/boiler-type", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/bedrooms"));
    }

    @Test
    void boilerPositionPage_shouldReturnBoilerPositionPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.BOILER_POSITION))).thenReturn(true);

        String view = controller.boilerPositionPage(session, model);

        assertEquals("boiler-installation-quote/boiler-position", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/boiler-type"));
    }

    @Test
    void boilerLocationPage_shouldReturnBoilerLocationPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.BOILER_LOCATION))).thenReturn(true);

        String view = controller.boilerLocationPage(session, model);

        assertEquals("boiler-installation-quote/boiler-location", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/boiler-position"));
    }

    @Test
    void bedroomsPage_shouldReturnBedroomsPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.BEDROOMS))).thenReturn(true);

        String view = controller.bedroomsPage(session, model);

        assertEquals("boiler-installation-quote/bedrooms", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/property-type"));
    }

    @Test
    void boilerConditionPage_shouldReturnBoilerConditionPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.BOILER_CONDITION))).thenReturn(true);

        String view = controller.boilerConditionPage(session, model);

        assertEquals("boiler-installation-quote/boiler-condition", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/boiler-location"));
    }

    @Test
    void relocationPage_shouldReturnRelocationPage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.RELOCATION))).thenReturn(true);

        String view = controller.relocationPage(session, model);

        assertEquals("boiler-installation-quote/relocation", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/boiler-condition"));
    }

    @Test
    void relocationDistancePage_shouldReturnRelocationDistancePage_whenStepAccessible() {
        when(sessionService.getState(session)).thenReturn(new QuoteSessionState());
        when(wizardService.canAccessStep(any(), eq(QuoteStep.RELOCATION_DISTANCE))).thenReturn(true);

        String view = controller.relocationDistancePage(session, model);

        assertEquals("boiler-installation-quote/relocation-distance", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/relocation"));
    }

    @Test
    void flueTypePage_shouldUseRelocationPageAsBackUrl_whenRelocationIsNo() {
        QuoteSessionState state = new QuoteSessionState();
        state.setRelocation(Relocation.NO);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FLUE_TYPE)).thenReturn(true);

        String view = controller.flueTypePage(session, model);

        assertEquals("boiler-installation-quote/flue-type", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/relocation"));
    }

    @Test
    void flueTypePage_shouldUseRelocationDistanceAsBackUrl_whenRelocationIsYes() {
        QuoteSessionState state = new QuoteSessionState();
        state.setRelocation(Relocation.YES);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FLUE_TYPE)).thenReturn(true);

        String view = controller.flueTypePage(session, model);

        assertEquals("boiler-installation-quote/flue-type", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/relocation-distance"));
    }

    @Test
    void flueLengthPage_shouldUseFlueTypeAsBackUrl() {
        QuoteSessionState state = new QuoteSessionState();

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FLUE_LENGTH)).thenReturn(true);

        String view = controller.flueLengthPage(session, model);

        assertEquals("boiler-installation-quote/flue-length", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/flue-type"));
        verify(model).addAttribute(eq("flueLengthImage"), eq("/images/flues/flue-length-vertical.svg"));
    }

    @Test
    void flueLengthPage_shouldUseHorizontalImage_forHorizontalFlue() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFlueType(FlueType.HORIZONTAL);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FLUE_LENGTH)).thenReturn(true);

        String view = controller.flueLengthPage(session, model);

        assertEquals("boiler-installation-quote/flue-length", view);
        verify(model).addAttribute(eq("flueLengthImage"), eq("/images/flues/flue-length-horizontal.svg"));
    }

    @Test
    void fluePositionPage_shouldUseFlueLengthAsBackUrl() {
        QuoteSessionState state = new QuoteSessionState();

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.FLUE_POSITION)).thenReturn(true);

        String view = controller.fluePositionPage(session, model);

        assertEquals("boiler-installation-quote/flue-position", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/flue-length"));
    }

    @Test
    void radiatorCountPage_shouldUseFluePropertyDistanceAsBackUrl_forHorizontalFlue() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFlueType(FlueType.HORIZONTAL);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.RADIATOR_COUNT)).thenReturn(true);

        String view = controller.radiatorCountPage(session, model);

        assertEquals("boiler-installation-quote/radiator-count", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/flue-property-distance"));
    }

    @Test
    void radiatorCountPage_shouldUseFlueLengthAsBackUrl_forVerticalFlue() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFlueType(FlueType.VERTICAL);

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.RADIATOR_COUNT)).thenReturn(true);

        String view = controller.radiatorCountPage(session, model);

        assertEquals("boiler-installation-quote/radiator-count", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/flue-length"));
    }

    @Test
    void bathShowerCountPage_shouldUseRadiatorCountAsBackUrl() {
        QuoteSessionState state = new QuoteSessionState();

        when(sessionService.getState(session)).thenReturn(state);
        when(wizardService.canAccessStep(state, QuoteStep.BATH_SHOWER_COUNT)).thenReturn(true);

        String view = controller.bathShowerCountPage(session, model);

        assertEquals("boiler-installation-quote/bath-shower-count", view);
        verify(model).addAttribute(eq("backUrl"), eq("/quote/radiator-count"));
    }

    @Test
    void summaryPage_shouldRedirectToQuote_whenStateIsIncomplete() {
        QuoteSessionState state = new QuoteSessionState();
        when(sessionService.getState(session)).thenReturn(state);

        String view = controller.summaryPage(session, model);

        assertEquals("redirect:/quote", view);
        verifyNoInteractions(boilerRecommendationService);
        verifyNoInteractions(quotePersistenceService);
    }

    @Test
    void summaryPage_shouldAddRecommendation_whenStateIsComplete() {
        QuoteSessionState state = completeVerticalState();
        BoilerModel boiler = new BoilerModel();
        boiler.setBrand("Main");
        boiler.setModel("Eco Compact 15kW");

        BoilerRecommendationResult recommendation = new BoilerRecommendationResult(
                BoilerType.HEAT_ONLY,
                "Heat-only boiler",
                5,
                1,
                true,
                java.util.List.of(boiler)
        );

        when(sessionService.getState(session)).thenReturn(state);
        when(sessionService.getSavedQuoteId(session)).thenReturn(null);
        when(boilerRecommendationService.recommend(state)).thenReturn(recommendation);
        when(quotePersistenceService.saveOrUpdate(null, "boiler-installation", state, recommendation, 0, 0, 0, 0))
                .thenReturn(101L);

        String view = controller.summaryPage(session, model);

        assertEquals("boiler-installation-quote/summary", view);
        verify(model).addAttribute("state", state);
        verify(model).addAttribute("boilerRecommendation", recommendation);
        verify(model).addAttribute("backUrl", "/quote/bath-shower-count");
        verify(model).addAttribute("recommendedBoilerFallbackImage", "/images/boilers/heat-only.svg");
        verify(model).addAttribute("recommendedBoilerExtraPriceGbp", 0);
        verify(quotePersistenceService).saveOrUpdate(null, "boiler-installation", state, recommendation, 0, 0, 0, 0);
        verify(sessionService).saveSavedQuoteId(session, 101L);
    }

    @Test
    void summaryPage_shouldAddSelectedExtrasToRecommendedBoilerPrice() {
        QuoteSessionState state = completeHorizontalStateWithExtras();
        BoilerRecommendationResult recommendation = new BoilerRecommendationResult(
                BoilerType.COMBI,
                "Combi boiler",
                5,
                2,
                false,
                java.util.List.of()
        );

        when(sessionService.getState(session)).thenReturn(state);
        when(sessionService.getSavedQuoteId(session)).thenReturn(null);
        when(boilerRecommendationService.recommend(state)).thenReturn(recommendation);
        when(relocationPricingService.getPrice(RelocationDistance.TWO_TO_THREE)).thenReturn(300);
        when(flueLengthPricingService.getPrice(FlueLength.FOUR_TO_FIVE)).thenReturn(500);
        when(fluePositionPricingService.getPrice(FluePosition.UNDER_STRUCTURE)).thenReturn(50);
        when(flueClearancePricingService.getPrice(FlueClearance.LESS_THAN_THIRTY_CM)).thenReturn(150);
        when(quotePersistenceService.saveOrUpdate(null, "boiler-installation", state, recommendation, 300, 500, 50, 150))
                .thenReturn(202L);

        String view = controller.summaryPage(session, model);

        assertEquals("boiler-installation-quote/summary", view);
        verify(model).addAttribute("relocationPriceGbp", 300);
        verify(model).addAttribute("flueLengthPriceGbp", 500);
        verify(model).addAttribute("fluePositionPriceGbp", 50);
        verify(model).addAttribute("flueClearancePriceGbp", 150);
        verify(model).addAttribute("recommendedBoilerExtraPriceGbp", 1000);
        verify(quotePersistenceService).saveOrUpdate(null, "boiler-installation", state, recommendation, 300, 500, 50, 150);
        verify(sessionService).saveSavedQuoteId(session, 202L);
    }

    @Test
    void contactPage_shouldReturnContactTemplate_whenBoilerExistsInRecommendation() {
        QuoteSessionState state = completeHorizontalStateWithExtras();
        BoilerModel boiler = new BoilerModel();
        boiler.setBrand("Vaillant");
        boiler.setModel("ecoTEC Plus 28kW Combi");
        boiler.setPowerKw(28);
        boiler.setAveragePriceGbp(2000);
        boiler.setImage("/images/boilers/catalog/vaillant-ecotec-plus-28kw-combi.jpg");
        BoilerRecommendationResult recommendation = new BoilerRecommendationResult(
                BoilerType.COMBI,
                "Combi boiler",
                9,
                2,
                true,
                java.util.List.of(boiler)
        );

        when(sessionService.getState(session)).thenReturn(state);
        when(sessionService.getSavedQuoteId(session)).thenReturn(77L);
        when(boilerRecommendationService.recommend(state)).thenReturn(recommendation);
        when(relocationPricingService.getPrice(RelocationDistance.TWO_TO_THREE)).thenReturn(300);
        when(flueLengthPricingService.getPrice(FlueLength.FOUR_TO_FIVE)).thenReturn(500);
        when(fluePositionPricingService.getPrice(FluePosition.UNDER_STRUCTURE)).thenReturn(50);
        when(flueClearancePricingService.getPrice(FlueClearance.LESS_THAN_THIRTY_CM)).thenReturn(150);
        when(quotePersistenceService.saveOrUpdate(77L, "boiler-installation", state, recommendation, 300, 500, 50, 150))
                .thenReturn(77L);

        String view = controller.contactPage("Vaillant ecoTEC Plus 28kW Combi", session, model);

        assertEquals("boiler-installation-quote/contact", view);
        verify(model).addAttribute(eq("contactRequest"), any(BoilerContactRequestDto.class));
        verify(model).addAttribute("contactSuccess", false);
        verify(model).addAttribute("backUrl", "/quote/summary");
        verify(model).addAttribute("selectedBoiler", boiler);
        verify(model).addAttribute("selectedBoilerLabel", "Vaillant ecoTEC Plus 28kW Combi");
        verify(model).addAttribute("selectedBoilerPriceGbp", 3000);
        verify(model).addAttribute("selectedBoilerImage", "/images/boilers/catalog/vaillant-ecotec-plus-28kw-combi.jpg");
    }

    @Test
    void submitContactRequest_shouldRedirectToContact_whenFormIsValid() {
        QuoteSessionState state = completeHorizontalStateWithExtras();
        BoilerModel boiler = new BoilerModel();
        boiler.setBrand("Vaillant");
        boiler.setModel("ecoTEC Plus 28kW Combi");
        boiler.setPowerKw(28);
        boiler.setAveragePriceGbp(2000);
        BoilerRecommendationResult recommendation = new BoilerRecommendationResult(
                BoilerType.COMBI,
                "Combi boiler",
                9,
                2,
                true,
                java.util.List.of(boiler)
        );
        BoilerContactRequestDto request = new BoilerContactRequestDto();
        request.setSelectedBoiler("Vaillant ecoTEC Plus 28kW Combi");
        request.setEmail("client@example.com");
        request.setPhone("+44 7700 900123");
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "contactRequest");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(sessionService.getState(session)).thenReturn(state);
        when(sessionService.getSavedQuoteId(session)).thenReturn(77L);
        when(boilerRecommendationService.recommend(state)).thenReturn(recommendation);
        when(relocationPricingService.getPrice(RelocationDistance.TWO_TO_THREE)).thenReturn(300);
        when(flueLengthPricingService.getPrice(FlueLength.FOUR_TO_FIVE)).thenReturn(500);
        when(fluePositionPricingService.getPrice(FluePosition.UNDER_STRUCTURE)).thenReturn(50);
        when(flueClearancePricingService.getPrice(FlueClearance.LESS_THAN_THIRTY_CM)).thenReturn(150);
        when(quotePersistenceService.saveOrUpdate(77L, "boiler-installation", state, recommendation, 300, 500, 50, 150))
                .thenReturn(77L);

        String view = controller.submitContactRequest(request, bindingResult, session, model, redirectAttributes);

        assertEquals("redirect:/quote/contact", view);
        verify(quotePersistenceService).saveContactDetails(77L, "Vaillant ecoTEC Plus 28kW Combi", "client@example.com", "+44 7700 900123");
        verify(quoteLeadEmailService).sendLeadEmails(state, "Vaillant ecoTEC Plus 28kW Combi", 3000, "client@example.com", "+44 7700 900123");
        assertEquals(true, redirectAttributes.getFlashAttributes().get("contactSuccess"));
        assertEquals("Vaillant ecoTEC Plus 28kW Combi", redirectAttributes.getFlashAttributes().get("selectedBoilerLead"));
        assertEquals("Vaillant ecoTEC Plus 28kW Combi", redirectAttributes.get("boiler"));
    }

    @Test
    void submitContactRequest_shouldReturnContactPage_whenFormHasErrors() {
        QuoteSessionState state = completeHorizontalStateWithExtras();
        BoilerModel boiler = new BoilerModel();
        boiler.setBrand("Vaillant");
        boiler.setModel("ecoTEC Plus 28kW Combi");
        boiler.setPowerKw(28);
        boiler.setAveragePriceGbp(2000);
        BoilerRecommendationResult recommendation = new BoilerRecommendationResult(
                BoilerType.COMBI,
                "Combi boiler",
                9,
                2,
                true,
                java.util.List.of(boiler)
        );
        BoilerContactRequestDto request = new BoilerContactRequestDto();
        request.setSelectedBoiler("Vaillant ecoTEC Plus 28kW Combi");
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "contactRequest");
        bindingResult.rejectValue("email", "NotBlank", "Please enter your email address.");

        when(sessionService.getState(session)).thenReturn(state);
        when(sessionService.getSavedQuoteId(session)).thenReturn(77L);
        when(boilerRecommendationService.recommend(state)).thenReturn(recommendation);
        when(relocationPricingService.getPrice(RelocationDistance.TWO_TO_THREE)).thenReturn(300);
        when(flueLengthPricingService.getPrice(FlueLength.FOUR_TO_FIVE)).thenReturn(500);
        when(fluePositionPricingService.getPrice(FluePosition.UNDER_STRUCTURE)).thenReturn(50);
        when(flueClearancePricingService.getPrice(FlueClearance.LESS_THAN_THIRTY_CM)).thenReturn(150);
        when(quotePersistenceService.saveOrUpdate(77L, "boiler-installation", state, recommendation, 300, 500, 50, 150))
                .thenReturn(77L);

        String view = controller.submitContactRequest(request, bindingResult, session, model, new RedirectAttributesModelMap());

        assertEquals("boiler-installation-quote/contact", view);
        verify(model).addAttribute("contactSuccess", false);
        verify(model).addAttribute("backUrl", "/quote/summary");
        verify(model).addAttribute("selectedBoiler", boiler);
        verify(model).addAttribute("selectedBoilerLabel", "Vaillant ecoTEC Plus 28kW Combi");
        verify(model).addAttribute("selectedBoilerPriceGbp", 3000);
        verifyNoInteractions(quoteLeadEmailService);
        verify(quotePersistenceService, never()).saveContactDetails(any(), any(), any(), any());
    }

    private QuoteSessionState completeVerticalState() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setFuel(FuelType.GAS);
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);
        state.setBedrooms(Bedrooms.ONE);
        state.setBoilerType(BoilerType.HEAT_ONLY);
        state.setBoilerPosition(BoilerPosition.FLOOR_STANDING);
        state.setBoilerLocation(BoilerLocation.GROUND_FLOOR);
        state.setBoilerCondition(BoilerCondition.OLD_INEFFICIENT);
        state.setRelocation(Relocation.NO);
        state.setFlueType(FlueType.VERTICAL);
        state.setVerticalFlueType(VerticalFlueType.SLOPED_ROOF);
        state.setFlueLength(com.kgboilers.model.boilerinstallation.enums.FlueLength.ZERO_TO_ONE);
        state.setRadiatorCount(RadiatorCount.ZERO_TO_FIVE);
        state.setBathShowerCount(BathShowerCount.ONE);
        return state;
    }

    private QuoteSessionState completeHorizontalStateWithExtras() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setFuel(FuelType.GAS);
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);
        state.setBedrooms(Bedrooms.THREE);
        state.setBoilerType(BoilerType.COMBI);
        state.setBoilerPosition(BoilerPosition.WALL_MOUNTED);
        state.setBoilerLocation(BoilerLocation.GROUND_FLOOR);
        state.setBoilerCondition(BoilerCondition.NOT_WORKING);
        state.setRelocation(Relocation.YES);
        state.setRelocationDistance(RelocationDistance.TWO_TO_THREE);
        state.setFlueType(FlueType.HORIZONTAL);
        state.setFlueLength(FlueLength.FOUR_TO_FIVE);
        state.setFluePosition(FluePosition.UNDER_STRUCTURE);
        state.setFlueClearance(FlueClearance.LESS_THAN_THIRTY_CM);
        state.setFluePropertyDistance(com.kgboilers.model.boilerinstallation.enums.FluePropertyDistance.MORE_THAN_ONE_METRE);
        state.setRadiatorCount(RadiatorCount.ZERO_TO_FIVE);
        state.setBathShowerCount(BathShowerCount.TWO);
        return state;
    }
}
