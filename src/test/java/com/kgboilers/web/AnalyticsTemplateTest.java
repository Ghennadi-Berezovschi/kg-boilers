package com.kgboilers.web;

import com.kgboilers.config.properties.AnalyticsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsTemplateTest {
    @Test
    void analyticsIsAbsentWithoutValidConfiguration() {
        assertThat(renderFooter("", false)).doesNotContain("analytics.js", "analytics-consent");
        assertThat(renderFooter("G-INVALID<script>", false)).doesNotContain("analytics.js", "G-INVALID");
    }

    @Test
    void rendersOnlyLocalConsentAssetsAndServerConfirmedLeadMarker() {
        String html = renderFooter("G-TEST123456", true);
        assertThat(html).contains("data-measurement-id=\"G-TEST123456\"", "data-lead-submitted=\"true\"",
                "Reject cookies", "Accept cookies", "analytics.js", "Cookie settings");
        assertThat(html).doesNotContain("googletagmanager.com", "google-analytics.com");
        assertThat(renderFooter("G-TEST123456", false)).contains("data-lead-submitted=\"false\"");
    }

    private String renderFooter(String measurementId, boolean submitted) {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var servletContext = new MockServletContext();
        var request = new MockHttpServletRequest(servletContext);
        var exchange = JakartaServletWebApplication.buildApplication(servletContext)
                .buildExchange(request, new MockHttpServletResponse());
        var context = new WebContext(exchange, Locale.UK);
        var properties = new AnalyticsProperties();
        properties.setMeasurementId(measurementId);
        context.setVariable("analytics", properties);
        context.setVariable("company", Map.of("name", "Pro Boiler Services London"));
        context.setVariable("contactSuccess", submitted);
        return engine.process("fragments/_footer", context);
    }
}
