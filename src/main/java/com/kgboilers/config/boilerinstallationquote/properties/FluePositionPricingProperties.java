package com.kgboilers.config.boilerinstallationquote.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kg.pricing.flue-position")
public record FluePositionPricingProperties(
        @NotNull @Min(0) Integer underStructure,
        @NotNull @Min(0) Integer openArea
) {}
