package com.kgboilers.config.boilerinstallationquote.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kg.location")
public class LocationProperties {

    @NotBlank
    private String postcode;

    @NotNull
    private Double maxDistanceMiles;

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Double getMaxDistanceMiles() {
        return maxDistanceMiles;
    }

    public void setMaxDistanceMiles(Double maxDistanceMiles) {
        this.maxDistanceMiles = maxDistanceMiles;
    }
}
