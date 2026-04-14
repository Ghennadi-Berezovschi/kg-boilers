package com.kgboilers.config.boilerinstallationquote.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kg.pricing.flue-clearance")
public record FlueClearancePricingProperties(
        @NotNull @Min(0) Integer thirtyCmOrMore,
        @NotNull @Min(0) Integer lessThanThirtyCm,
        @NotNull @Min(0) Integer unsure
) {}
