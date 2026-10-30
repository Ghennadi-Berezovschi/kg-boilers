package com.kgboilers.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgboilers.model.admin.AdminAnswerSection;
import com.kgboilers.model.admin.AdminDetailRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAnswerFormatterTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void formatsBoilerOptionsAndPricesWithoutRawJson() throws Exception {
        var sections = AdminAnswerFormatter.format(mapper.readTree("""
                {"answers":{"propertyType":"DETACHED","boilerType":"SYSTEM","hotWaterAvailable":false},
                 "pricing":{"flueLengthPriceGbp":0,"installationPriceGbp":2040},
                 "recommendation":{"exactMatch":true,"boilers":[
                   {"brand":"Main","model":"Eco Compact","powerKw":18,"totalPriceGbp":2040,"image":"/images/main.png"},
                   {"brand":"Vaillant","model":"ecoTEC Plus","powerKw":24,"totalPriceGbp":2680}]}}
                """));

        assertThat(section(sections, "Property & household").rows())
                .contains(new AdminDetailRow("Property type", "Detached"));
        assertThat(section(sections, "Boiler & fuel").rows())
                .contains(new AdminDetailRow("Hot water available", "No"));
        assertThat(section(sections, "Price breakdown").rows())
                .contains(new AdminDetailRow("Additional flue length", "£0"),
                        new AdminDetailRow("Installation quote total", "£2,040"));
        assertThat(section(sections, "Boiler option 1").rows())
                .contains(new AdminDetailRow("Brand", "Main"), new AdminDetailRow("Output", "18 kW"));
        assertThat(section(sections, "Boiler option 2").rows())
                .contains(new AdminDetailRow("Model", "ecoTEC Plus"), new AdminDetailRow("Total price", "£2,680"));
        assertThat(section(sections, "Recommendation details").rows())
                .contains(new AdminDetailRow("Exact match available", "Yes"));
        assertThat(sections.toString()).doesNotContain("/images/", "{\"brand\"");
    }

    @Test
    void supportsFlatHeatingAnswersAndSeparateInstallationItems() throws Exception {
        var sections = AdminAnswerFormatter.format(mapper.readTree("""
                {"postcode":"E16 4JJ","radiatorIssues":["Leaking","Cold at the top",null],
                 "valveQuantities":{"trvValves":2},
                 "installationItems":[{"type":"Radiator","quantity":2,"lengthMm":600},{"type":"Pump","quantity":1}],
                 "uploadedPictures":[{"url":"/uploads/private.jpg"}],"jobPictures":[],
                 "selectedOptionalExtras":[{"title":"Filter"}]}
                """));

        assertThat(section(sections, "Heating & repair").rows())
                .contains(new AdminDetailRow("Radiator issues", "Leaking, Cold at the top"));
        assertThat(section(sections, "Valve quantities").rows()).contains(new AdminDetailRow("TRV valves", "2"));
        assertThat(section(sections, "Installation item 1").rows()).contains(new AdminDetailRow("Length", "600 mm"));
        assertThat(section(sections, "Installation item 2").rows()).contains(new AdminDetailRow("Type", "Pump"));
        assertThat(sections.toString()).doesNotContain("private.jpg", "Filter");
    }

    @Test
    void preservesUnrecognisedFieldsAndLiteralCustomerText() throws Exception {
        var sections = AdminAnswerFormatter.format(mapper.readTree("""
                {"answers":{"problemDetails":"<script>alert('test')</script>","newQuestion":"Answer"},
                 "futureData":{"notes":"Keep this detail"},"newItems":[{"name":"Part A"},{"name":"Part B"}],
                 "empty":[],"missing":null}
                """));
        assertThat(section(sections, "Heating & repair").rows())
                .contains(new AdminDetailRow("Problem details", "<script>alert('test')</script>"));
        assertThat(section(sections, "Request details").rows())
                .contains(new AdminDetailRow("New question", "Answer"), new AdminDetailRow("Future data notes", "Keep this detail"));
        assertThat(section(sections, "New items · 2").rows()).contains(new AdminDetailRow("Name", "Part B"));
        assertThat(AdminAnswerFormatter.format(mapper.readTree("{}"))).isEmpty();
    }

    private AdminAnswerSection section(List<AdminAnswerSection> sections, String title) {
        return sections.stream().filter(section -> section.title().equals(title)).findFirst().orElseThrow();
    }
}
