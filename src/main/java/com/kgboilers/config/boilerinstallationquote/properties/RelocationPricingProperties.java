package com.kgboilers.config.boilerinstallationquote.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kg.pricing.relocation")
public record RelocationPricingProperties(
        @NotNull @Min(0) Integer zeroToOne,
        @NotNull @Min(0) Integer twoToThree,
        @NotNull @Min(0) Integer fourToFive,
        @NotNull @Min(0) Integer sixToSeven,
        @NotNull @Min(0) Integer sevenPlus
) {}
