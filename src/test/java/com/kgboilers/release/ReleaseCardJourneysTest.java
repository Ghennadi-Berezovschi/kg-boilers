package com.kgboilers.release;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgboilers.dto.boilerinstallationquote.BoilerContactRequestDto;
import com.kgboilers.model.Coordinates;
import com.kgboilers.model.boilerinstallationquote.BoilerRecommendationResult;
import com.kgboilers.service.PostcodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Real MVC, Thymeleaf, validation, PostgreSQL migrations and lead persistence.
 * Run only against a disposable QA database. Email and postcode transport are mocked.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=${KG_RELEASE_QA_DB_URL}",
        "spring.datasource.username=${KG_RELEASE_QA_DB_USERNAME}",
        "spring.datasource.password=${KG_RELEASE_QA_DB_PASSWORD}",
        "kg.uploads.quote-pictures-dir=${java.io.tmpdir}/kg-boilers-release-qa-pictures"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "KG_RELEASE_QA_DB_URL", matches = ".*kg_boilers_release_qa_.*")
class ReleaseCardJourneysTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @MockBean PostcodeService postcodes;
    @MockBean JavaMailSender mail;

    record Scenario(String name, String entry, Map<String, Object> overrides) {
        @Override public String toString() { return name; }
    }
    record Journey(MockHttpSession session, String path, MvcResult page) {}

    @BeforeEach
    void isolateTransport() {
        when(postcodes.getCoordinates(anyString())).thenReturn(new Coordinates(51.51, 0.01));
        clearInvocations(mail);
    }

    static Stream<Scenario> journeys() {
        return Stream.of(
                scenario("Installation: combi", "/quote?service=boiler-installation"),
                scenario("Installation: system", "/quote?service=boiler-installation", "boiler-type", Map.of("boilerType", "system")),
                scenario("Installation: heat only", "/quote?service=boiler-installation", "boiler-type", Map.of("boilerType", "heat-only")),
                new Scenario("Installation: conversion and relocation", "/quote?service=boiler-installation", Map.of(
                        "boiler-type", Map.of("boilerType", "heat-only"),
                        "boiler-conversion", Map.of("conversion", "yes"),
                        "relocation", Map.of("relocation", "yes"),
                        "flue-shape", Map.of("flueShape", "square"))),
                scenario("Installation: sloped roof", "/quote?service=boiler-installation", "flue-type", Map.of("flueType", "vertical", "verticalFlueType", "sloped-roof")),
                scenario("Installation: flat roof", "/quote?service=boiler-installation", "flue-type", Map.of("flueType", "vertical", "verticalFlueType", "flat-roof")),
                scenario("Installation: electric", "/quote?service=boiler-installation", "fuel-type", Map.of("fuel", "electric")),
                scenario("Installation: identify boiler", "/quote?service=boiler-installation", "boiler-type", Map.of("boilerType", "other")),
                scenario("Boiler repair: fault code", "/boiler-repair-quote"),
                scenario("Boiler repair: hot water", "/boiler-repair-quote", "repair-problem", Map.of("repairProblem", "hot-water")),
                scenario("Boiler service", "/gas-safety-certificate-quote", "service-type", Map.of("serviceType", "boiler-service")),
                scenario("Boiler service and CP12", "/gas-safety-certificate-quote"),
                scenario("CP12 only", "/gas-safety-certificate-quote", "service-type", Map.of("serviceType", "gas-safety-certificate")),
                scenario("Central heating: radiator and valves", "/central-heating-quote"),
                new Scenario("Central heating: new towel rail", "/central-heating-quote", Map.of(
                        "installation-item", Map.of("installationItemType", "towel-rail"),
                        "installation-position", Map.of("installationPositionType", "no-existing-item"))),
                scenario("Central heating: repair", "/central-heating-quote", "radiator-issues", Map.of("radiatorIssues", new String[]{"all-radiators-are-cold"})),
                scenario("Hot water cylinder", "/quote?service=hot-water-cylinder", "boiler-type", Map.of("boilerType", "system")),
                scenario("Cylinder: no gas boiler", "/quote?service=hot-water-cylinder", "boiler-type", Map.of("boilerType", "other")),
                scenario("Gas pipework", "/quote?service=gas-pipework-and-gas-leak-detection"),
                scenario("Gas cooker", "/quote?service=gas-cooker-and-hob-installation"),
                scenario("Gas hob", "/quote?service=gas-cooker-and-hob-installation", "problem-details", Map.of("problemDetails", "QA installation request", "installationAppliance", "gas-hob")),
                scenario("Plumbing", "/quote?service=plumbing"),
                scenario("Bathroom", "/quote?service=bathroom-refurbishment"),
                scenario("Air conditioning: multiple units", "/quote?service=air-conditioning-installation")
        );
    }

    static Scenario scenario(String name, String entry) { return new Scenario(name, entry, Map.of()); }
    static Scenario scenario(String name, String entry, String step, Object answer) { return new Scenario(name, entry, Map.of(step, answer)); }

    @ParameterizedTest(name = "{0}")
    @MethodSource("journeys")
    void everyCardRendersValidatesPersistsAndConfirms(Scenario scenario) throws Exception {
        Journey journey = walk(scenario);
        String path = journey.path();
        MvcResult page = journey.page();
        if (path.equals("/quote/summary")) {
            BoilerRecommendationResult recommendation = (BoilerRecommendationResult) page.getModelAndView().getModel().get("boilerRecommendation");
            assertThat(recommendation.getBoilers()).as(scenario.name() + " recommendations").isNotEmpty();
            // Deliberately choose a non-primary model to catch price/selection mismatches.
            var selected = recommendation.getBoilers().getLast();
            String label = selected.getBrand() + " " + selected.getModel();
            page = render(get("/quote/contact").param("boiler", label).session(journey.session()));
            path = "/quote/contact";
        }

        String action = path.equals("/boiler-repair-quote/summary") ? "/boiler-repair-quote/repair-contact"
                : path.equals("/central-heating-quote/summary") ? "/central-heating-quote/contact"
                : path.equals("/gas-safety-certificate-quote/summary") ? "/gas-safety-certificate-quote/contact" : "/quote/contact";
        var contact = page.getModelAndView().getModel().get("contactRequest");
        String selection = contact instanceof BoilerContactRequestDto dto ? dto.getSelectedBoiler() : null;
        String email = "release-" + UUID.randomUUID() + "@example.invalid";
        int before = count(email);
        MockHttpServletRequestBuilder invalid = post(action).session(journey.session())
                .param("name", "").param("email", "bad-email").param("phone", "abc");
        if (selection != null) invalid.param("selectedBoiler", selection);
        MvcResult invalidResult = render(invalid);
        assertThat(invalidResult.getResponse().getContentAsString()).contains("Please enter");
        assertThat(count(email)).isEqualTo(before);
        verifyNoInteractions(mail);

        MockHttpServletRequestBuilder oversized = post(action).session(journey.session())
                .param("name", "Q".repeat(256)).param("email", email).param("phone", "+447700900123");
        if (selection != null) oversized.param("selectedBoiler", selection);
        assertThat(render(oversized).getResponse().getContentAsString()).contains("Name is too long");
        MockHttpServletRequestBuilder punctuationPhone = post(action).session(journey.session())
                .param("name", "Release QA").param("email", email).param("phone", "-------");
        if (selection != null) punctuationPhone.param("selectedBoiler", selection);
        assertThat(render(punctuationPhone).getResponse().getContentAsString()).contains("Please enter a valid phone");
        verifyNoInteractions(mail);

        MockHttpServletRequestBuilder submit = post(action).session(journey.session())
                .param("name", "Release QA").param("email", email).param("phone", "+447700900123");
        if (selection != null) submit.param("selectedBoiler", selection);
        boolean visitExtras = !action.equals("/quote/contact");
        if (visitExtras) submit.param("selectedExtras", "Power flushing", "Magnetic filter", "Power flushing", "forged-extra");
        MvcResult result = mvc.perform(submit).andReturn();
        assertThat(result.getResponse().getStatus()).as(scenario.name() + " submit").isEqualTo(302);
        assertThat(count(email)).as(scenario.name() + " persisted lead").isEqualTo(1);
        var saved = jdbc.queryForMap("select id, selected_boiler, client_name, client_answers_json::text, installation_price_gbp, optional_extras_price_gbp, selected_optional_extras_json::text from quotes where client_email = ?", email);
        assertThat(saved.get("client_name")).isEqualTo("Release QA");
        assertThat(saved.get("client_answers_json")).isNotNull();
        if (path.equals("/quote/contact") && page.getModelAndView().getModel().get("selectedBoilerPriceGbp") instanceof Integer price && price > 0) {
            assertThat(saved.get("installation_price_gbp")).as(scenario.name() + " saved price matches selected price").isEqualTo(price);
        }
        JsonNode snapshot = json.readTree(saved.get("client_answers_json").toString());
        if (visitExtras) {
            assertThat(saved.get("optional_extras_price_gbp")).isEqualTo(340);
            assertThat(json.readTree(saved.get("selected_optional_extras_json").toString()).size()).isEqualTo(2);
            assertThat(snapshot.path("selectedOptionalExtras").size()).isEqualTo(2);
        }
        if (action.startsWith("/gas-safety")) {
            assertThat(snapshot.path("answers").path("gasSafetyServiceType").asText()).isNotBlank();
            if (!scenario.name().equals("Boiler service")) {
                assertThat(snapshot.path("answers").path("gasAppliances").asText()).isNotBlank();
            }
        }
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "release-qa", "", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        MockHttpSession admin = new MockHttpSession();
        admin.setAttribute("SPRING_SECURITY_CONTEXT", new org.springframework.security.core.context.SecurityContextImpl(authentication));
        assertThat(render(get("/admin/quotes/" + saved.get("id")).session(admin)).getResponse().getContentAsString())
                .contains(email).contains("Release QA");
        assertThat(render(get("/admin/quotes").param("q", email).session(admin)).getResponse().getContentAsString()).contains(email);
        verify(mail, timeout(3000).times(2)).send(any(SimpleMailMessage.class));
        MvcResult success = render(get(java.net.URI.create(result.getResponse().getRedirectedUrl())).session(journey.session()).flashAttrs(result.getFlashMap()));
        assertThat(success.getResponse().getContentAsString()).containsIgnoringCase("received");
    }

    @Test
    void allServiceCardLinksAndStaticImagesResolve() throws Exception {
        MvcResult home = render(get("/"));
        String html = home.getResponse().getContentAsString();
        for (String entry : journeys().map(Scenario::entry).distinct().toList()) {
            assertThat(html).contains(entry.replace("&", "&amp;"));
            render(get(entry).session(new MockHttpSession()));
        }
    }

    @Test
    void missingSessionAndInvalidSelectionCannotCreateLeads() throws Exception {
        for (String base : new String[]{"/quote", "/boiler-repair-quote", "/central-heating-quote", "/gas-safety-certificate-quote"}) {
            var result = mvc.perform(post(base + "/property-type").contentType(MediaType.APPLICATION_JSON).content("{\"propertyType\":\"house\"}")).andReturn();
            assertThat(result.getResponse().getStatus()).isBetween(400, 499);
            assertThat(result.getResponse().getContentAsString()).contains("SESSION_EXPIRED");
        }
        verifyNoInteractions(mail);
    }

    @Test
    void malformedOversizedAndOutOfAreaRequestsAreHandledAcrossAllCards() throws Exception {
        when(postcodes.getCoordinates("SW1A 1AA")).thenReturn(new Coordinates(54.0, -2.0));
        for (Scenario scenario : journeys().distinct().toList()) {
            MockHttpSession session = new MockHttpSession();
            render(get(scenario.entry()).session(session));
            String base = scenario.entry().split("\\?")[0];
            String start = base + "/start" + (scenario.entry().contains("?") ? "?" + scenario.entry().split("\\?", 2)[1] : "");
            for (String postcode : new String[]{"", "!!!!"}) {
                var invalid = mvc.perform(post(start).session(session).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("postcode", postcode)))).andReturn();
                assertThat(invalid.getResponse().getStatus()).as(scenario.name() + " invalid postcode").isBetween(400, 499);
            }
            var out = mvc.perform(post(start).session(session).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"postcode\":\"SW1A 1AA\"}")).andReturn();
            assertThat(out.getResponse().getStatus()).as(scenario.name() + " out of area").isEqualTo(403);
            assertThat(out.getResponse().getContentAsString()).contains("OUT_OF_AREA");
            var malformed = mvc.perform(post(start).session(session).contentType(MediaType.APPLICATION_JSON).content("{")).andReturn();
            assertThat(malformed.getResponse().getStatus()).isEqualTo(400);
            var oversized = mvc.perform(post(start).session(session).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"postcode\":\"" + "E".repeat(20000) + "\"}")).andReturn();
            assertThat(oversized.getResponse().getStatus()).isEqualTo(413);
        }
        verifyNoInteractions(mail);
    }

    @Test
    void callbackValidatesPersistsAndUsesCsrfProtection() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MvcResult page = render(get("/callback").session(session));
        var token = (org.springframework.security.web.csrf.CsrfToken) page.getRequest().getAttribute("_csrf");
        assertThat(token).isNotNull();
        String value = token.getToken();
        jakarta.servlet.http.Cookie cookie = page.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        var rejected = mvc.perform(post("/callback").session(session).param("name", "QA").param("phone", "+447700900123")).andReturn();
        assertThat(rejected.getResponse().getStatus()).isEqualTo(403);
        render(post("/callback").session(session).cookie(cookie).param(token.getParameterName(), value).param("name", "").param("phone", "-------"));
        verifyNoInteractions(mail);
        String email = "callback-" + UUID.randomUUID() + "@example.invalid";
        MvcResult result = render(post("/callback").session(session).cookie(cookie).param(token.getParameterName(), value)
                .param("name", "Release QA callback").param("email", email).param("phone", "+447700900123")
                .param("service", "Boiler repair").param("message", "QA callback request"));
        assertThat(result.getModelAndView().getModel().get("callbackSuccess")).isEqualTo(true);
        assertThat(count(email)).isEqualTo(1);
        verify(mail, timeout(3000).times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void invalidModelAndIncompleteContactsDoNotSaveOrSend() throws Exception {
        for (String path : new String[]{"/quote/contact", "/boiler-repair-quote/repair-contact", "/central-heating-quote/contact", "/gas-safety-certificate-quote/contact"}) {
            var result = mvc.perform(post(path).session(new MockHttpSession()).param("name", "QA")
                    .param("email", "expired@example.invalid").param("phone", "+447700900123")).andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(302);
        }
        Journey journey = walk(scenario("Unknown selection", "/quote?service=boiler-installation"));
        var result = mvc.perform(post("/quote/contact").session(journey.session()).param("selectedBoiler", "forged boiler")
                .param("name", "QA").param("email", "forged@example.invalid").param("phone", "+447700900123")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(302);
        assertThat(count("forged@example.invalid")).isZero();
        verifyNoInteractions(mail);
    }

    @Test
    void installationExtrasPicturesAndResubmissionKeepOneConsistentLead() throws Exception {
        Journey journey = walk(scenario("Installation extras", "/quote?service=boiler-installation", "boiler-type", Map.of("boilerType", "heat-only")));
        var recommendation = (BoilerRecommendationResult) journey.page().getModelAndView().getModel().get("boilerRecommendation");
        var boiler = recommendation.getBoilers().getLast();
        String label = boiler.getBrand() + " " + boiler.getModel();
        String[] extras = {"heat-only-2-port-position-valve", "heat-only-2-port-position-valve", "chemical-power-flush", "chemical-power-flush", "forged-extra"};
        MvcResult contact = render(get("/quote/contact").session(journey.session()).param("boiler", label).param("selectedExtras", extras));
        int displayedPrice = (Integer) contact.getModelAndView().getModel().get("selectedBoilerPriceGbp");
        String email = "attachment-" + UUID.randomUUID() + "@example.invalid";
        var badFile = new org.springframework.mock.web.MockMultipartFile("pictures", "document.txt", "text/plain", "QA".getBytes());
        MvcResult invalid = render(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/quote/contact")
                .file(badFile).session(journey.session()).param("selectedBoiler", label).param("selectedExtras", extras)
                .param("name", "Release QA").param("email", email).param("phone", "+447700900123"));
        var errors = (org.springframework.validation.BindingResult) invalid.getModelAndView().getModel().get("org.springframework.validation.BindingResult.contactRequest");
        assertThat(errors.getFieldError("pictures").getDefaultMessage()).contains("JPG, PNG or WebP");
        assertThat(count(email)).isZero();
        verifyNoInteractions(mail);
        byte[] png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aXxoAAAAASUVORK5CYII=");
        var file = new org.springframework.mock.web.MockMultipartFile("pictures", "qa.png", "image/png", png);
        var result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/quote/contact")
                .file(file).session(journey.session()).param("selectedBoiler", label).param("selectedExtras", extras)
                .param("name", "Release QA").param("email", email).param("phone", "+447700900123")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(302);
        assertThat(count(email)).isEqualTo(1);
        var saved = jdbc.queryForMap("select id, installation_price_gbp, optional_extras_price_gbp, client_answers_json::text from quotes where client_email = ?", email);
        assertThat(saved.get("installation_price_gbp")).isEqualTo(displayedPrice);
        assertThat(saved.get("optional_extras_price_gbp")).isEqualTo(99 * 2 + 195);
        JsonNode snapshot = json.readTree(saved.get("client_answers_json").toString());
        assertThat(snapshot.path("selectedOptionalExtras").size()).isEqualTo(2);
        assertThat(snapshot.path("pricing").path("installationPriceGbp").asInt()).isEqualTo(displayedPrice);
        assertThat(snapshot.path("uploadedPictures").size()).isEqualTo(1);
        String imageUrl = snapshot.path("uploadedPictures").get(0).path("url").asText();
        assertThat(mvc.perform(get(imageUrl)).andReturn().getResponse().getContentAsByteArray()).isEqualTo(png);
        verify(mail, timeout(3000).times(2)).send(any(SimpleMailMessage.class));
        // A repeated submission updates this request rather than inserting a duplicate.
        var repeat = mvc.perform(post("/quote/contact").session(journey.session()).param("selectedBoiler", label).param("selectedExtras", extras)
                .param("name", "Release QA updated").param("email", email).param("phone", "+447700900123")).andReturn();
        assertThat(repeat.getResponse().getStatus()).isEqualTo(302);
        assertThat(count(email)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select installation_price_gbp from quotes where id = ?", Integer.class, saved.get("id"))).isEqualTo(displayedPrice);
        assertThat(json.readTree(jdbc.queryForObject("select client_answers_json::text from quotes where id = ?", String.class, saved.get("id"))).path("uploadedPictures").size()).isEqualTo(1);
        verify(mail, timeout(3000).times(4)).send(any(SimpleMailMessage.class));
    }

    private Journey walk(Scenario scenario) throws Exception {
        MockHttpSession session = new MockHttpSession();
        render(get(scenario.entry()).session(session));
        String base = scenario.entry().split("\\?")[0];
        String start = base + "/start" + (scenario.entry().contains("?") ? "?" + scenario.entry().split("\\?", 2)[1] : "");
        String path = next(start, Map.of("postcode", "E15 4DH"), session);
        Set<String> terminal = Set.of("/quote/summary", "/quote/contact", "/boiler-repair-quote/summary", "/gas-safety-certificate-quote/summary", "/central-heating-quote/summary");
        for (int i = 0; i < 45; i++) {
            MvcResult page = render(get(path).session(session));
            if (terminal.contains(path)) return new Journey(session, path, page);
            Object back = page.getModelAndView().getModel().get("backUrl");
            if (back instanceof String backUrl) {
                var backPage = mvc.perform(get(backUrl).session(session)).andReturn();
                assertThat(backPage.getResponse().getStatus()).as(path + " back").isBetween(200, 399);
            }
            String step = path.substring(path.lastIndexOf('/') + 1);
            Object body = scenario.overrides().getOrDefault(step, answers().get(step));
            assertThat(body).as("Missing test answer for " + scenario.name() + " " + path).isNotNull();
            String endpoint = step.equals("fuel-type") ? base + "/fuel" : path;
            path = next(endpoint, body, session);
        }
        throw new AssertionError("Wizard loop: " + scenario.name() + " at " + path);
    }

    private String next(String path, Object body, MockHttpSession session) throws Exception {
        var result = mvc.perform(post(path).session(session).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))).andReturn();
        JsonNode response = json.readTree(result.getResponse().getContentAsString());
        assertThat(result.getResponse().getStatus()).as(path + " " + response).isEqualTo(200);
        assertThat(response.path("success").asBoolean()).as(path + " " + response).isTrue();
        return response.path("nextStep").asText();
    }

    private MvcResult render(MockHttpServletRequestBuilder request) throws Exception {
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getStatus()).as(request.toString()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).contains("<html").doesNotContain("Whitelabel Error Page");
        return result;
    }

    private int count(String email) { return jdbc.queryForObject("select count(*) from quotes where client_email = ?", Integer.class, email); }

    private Map<String, Object> answers() {
        Map<String, Object> a = new HashMap<>();
        a.put("fuel-type", Map.of("fuel", "gas"));
        a.put("property-ownership", Map.of("ownership", "homeowner"));
        a.put("property-type", Map.of("propertyType", "house"));
        a.put("bedrooms", Map.of("bedrooms", "three"));
        a.put("boiler-type", Map.of("boilerType", "combi"));
        a.put("boiler-make", Map.of("boilerMake", "vaillant"));
        a.put("boiler-conversion", Map.of("conversion", "no"));
        a.put("boiler-position", Map.of("boilerPosition", "wall-mounted"));
        a.put("boiler-location", Map.of("location", "kitchen"));
        a.put("boiler-floor-level", Map.of("floorLevel", "ground-floor"));
        a.put("boiler-condition", Map.of("boilerCondition", "old-inefficient"));
        a.put("relocation", Map.of("relocation", "no"));
        a.put("relocation-distance", Map.of("relocationDistance", "2-3"));
        a.put("flue-type", Map.of("flueType", "horizontal"));
        a.put("flue-shape", Map.of("flueShape", "round"));
        a.put("flue-length", Map.of("flueLength", "0-1"));
        a.put("flue-position", Map.of("fluePosition", "open-area"));
        a.put("flue-clearance", Map.of("flueClearance", "yes"));
        a.put("flue-property-distance", Map.of("fluePropertyDistance", "more-than-1m"));
        a.put("sloped-roof-position", Map.of("roofPosition", "highest-two-thirds"));
        a.put("radiator-count", Map.of("radiatorCount", "6-9"));
        a.put("bath-shower-count", Map.of("bathShowerCount", "1"));
        a.put("boiler-age", Map.of("boilerAge", "5-10-years"));
        a.put("repair-problem", Map.of("repairProblem", "heating"));
        a.put("boiler-pressure", Map.of("boilerPressureStatus", "no-pressure-normal"));
        a.put("fault-code", Map.of("faultCodeDisplayStatus", "yes-showing"));
        a.put("fault-code-details", Map.of("faultCodeDetails", "F22"));
        a.put("power-flush", Map.of("powerFlushStatus", "yes-done"));
        a.put("magnetic-filter", Map.of("magneticFilterStatus", "yes-has"));
        a.put("service-type", Map.of("serviceType", "boiler-service-and-gas-safety-certificate"));
        a.put("gas-appliances", Map.of("appliances", new Object[]{Map.of("appliance", "gas-boiler", "quantity", 2), Map.of("appliance", "gas-hob", "quantity", 1)}));
        a.put("hot-water", Map.of("hotWaterAvailable", false));
        a.put("problem-details", Map.of("problemDetails", "Release QA request", "installationAppliance", "gas-cooker"));
        a.put("plumbing-problems", Map.of("problems", new String[]{"leaking-tap", "leaking-pipe"}));
        a.put("air-conditioning-type", Map.of("airConditioningInstallationTypes", new String[]{"bedroom-installation", "living-room-installation"}));
        a.put("air-conditioning-room-size", Map.of("roomSizes", new Object[]{Map.of("roomSize", "up-to-15-sqm", "quantity", 2)}));
        a.put("air-conditioning-catalog", Map.of("airConditioningUnits", new Object[]{Map.of("airConditioningUnit", "daikin-sensira-2-5", "quantity", 2)}));
        a.put("radiator-issues", Map.of("radiatorIssues", new String[]{"install-radiator-or-towel-rail", "install-trv-valves"}));
        a.put("issue-details", Map.of("issueDetails", "Release QA heating request"));
        a.put("trv-valves", Map.of("trvValveStatus", "yes"));
        a.put("trv-installation-quantity", Map.of("trvValvesQuantity", 2, "lockshieldValvesQuantity", 1, "towelRailValvesQuantity", 1));
        a.put("installation-item", Map.of("installationItemType", "radiator"));
        a.put("installation-position", Map.of("installationPositionType", "same-position"));
        a.put("installation-move-distance", Map.of("moveDistance", "2-3"));
        a.put("installation-pipe-distance", Map.of("pipeDistance", "2-3"));
        a.put("radiator-specification", Map.of("radiatorConvectorType", "double-convector", "radiatorLengthMm", 1000, "radiatorWidthMm", 600, "towelRailLengthMm", 1000, "towelRailWidthMm", 500, "installationQuantity", 2));
        a.put("add-another-installation", Map.of("addAnother", false));
        return a;
    }
}
