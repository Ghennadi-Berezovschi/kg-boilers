package com.kgboilers.service.admin;

import com.kgboilers.model.admin.AdminAnswerSection;
import com.kgboilers.model.admin.AdminDetailRow;
import com.kgboilers.model.admin.AdminQuoteDetail;
import com.kgboilers.model.admin.AdminQuoteExtra;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdminQuoteDetailTemplateTest {

    @Test
    void rendersGroupedAnswersAndPreservesStatusAndUploadForms() {
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
        context.setVariable("company", Map.of("name", "Pro Boiler Services London"));
        context.setVariable("quote", new AdminQuoteDetail(16L, "18 Oct 2026, 14:00", "18 Oct 2026, 14:00",
                "Not sent yet", "NEW_LEAD", "Boiler Installation", "Customer", null, null, "E16 4JJ",
                "Main Eco Compact", 2040, List.of(new AdminQuoteExtra("Thermostat", 1, 220)), List.of(), List.of(),
                List.of(new AdminAnswerSection("Boiler option 1", List.of(
                        new AdminDetailRow("Model", "Main Eco Compact"),
                        new AdminDetailRow("Customer notes", "<script>alert('test')</script>"))))));

        String html = engine.process("admin/quote-detail", context);

        assertThat(html).contains("Boiler option 1", "Main Eco Compact", "£2,040", "New Lead",
                "action=\"/admin/quotes/16/status\"", "action=\"/admin/quotes/16/job-photos\"",
                "&lt;script&gt;", "Not sent yet");
        assertThat(html).doesNotContain("<script>alert('test')</script>");
    }
}
