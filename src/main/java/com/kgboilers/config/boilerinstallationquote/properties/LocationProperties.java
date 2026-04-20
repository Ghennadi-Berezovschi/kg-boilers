package com.kgboilers.config.boilerinstallationquote.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Validated
@ConfigurationProperties(prefix = "kg.location")
public class LocationProperties {

    @NotBlank
    private String postcode;

    private List<String> postcodes = new ArrayList<>();

    @NotNull
    private Double maxDistanceMiles;

    private Map<String, Double> serviceMaxDistanceMiles = new HashMap<>();
    private Map<String, List<String>> servicePostcodes = new HashMap<>();

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public List<String> getPostcodes() {
        return postcodes;
    }

    public void setPostcodes(List<String> postcodes) {
        this.postcodes = postcodes;
    }

    public Double getMaxDistanceMiles() {
        return maxDistanceMiles;
    }

    public void setMaxDistanceMiles(Double maxDistanceMiles) {
        this.maxDistanceMiles = maxDistanceMiles;
    }

    public Map<String, Double> getServiceMaxDistanceMiles() {
        return serviceMaxDistanceMiles;
    }

    public void setServiceMaxDistanceMiles(Map<String, Double> serviceMaxDistanceMiles) {
        this.serviceMaxDistanceMiles = serviceMaxDistanceMiles;
    }

    public Map<String, List<String>> getServicePostcodes() {
        return servicePostcodes;
    }

    public void setServicePostcodes(Map<String, List<String>> servicePostcodes) {
        this.servicePostcodes = servicePostcodes;
    }
}
