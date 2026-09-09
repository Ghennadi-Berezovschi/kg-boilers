package com.kgboilers.config.boilerinstallationquote.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "kg.air-conditioning.catalog")
public class AirConditioningCatalogProperties {

    private Map<String, Integer> purchasePricesGbp = new LinkedHashMap<>();
    private Map<String, Integer> standardInstallationWorksPricesGbp = new LinkedHashMap<>();
    private Map<String, Integer> standardExtraMaterialsPricesGbp = new LinkedHashMap<>();
    private Map<String, Integer> warrantyYears = new LinkedHashMap<>();
    private Map<String, String> imageUrls = new LinkedHashMap<>();

    public Map<String, Integer> getPurchasePricesGbp() {
        return purchasePricesGbp;
    }

    public void setPurchasePricesGbp(Map<String, Integer> purchasePricesGbp) {
        this.purchasePricesGbp = purchasePricesGbp;
    }

    public Map<String, Integer> getStandardInstallationWorksPricesGbp() {
        return standardInstallationWorksPricesGbp;
    }

    public void setStandardInstallationWorksPricesGbp(Map<String, Integer> standardInstallationWorksPricesGbp) {
        this.standardInstallationWorksPricesGbp = standardInstallationWorksPricesGbp;
    }

    public Map<String, Integer> getStandardExtraMaterialsPricesGbp() {
        return standardExtraMaterialsPricesGbp;
    }

    public void setStandardExtraMaterialsPricesGbp(Map<String, Integer> standardExtraMaterialsPricesGbp) {
        this.standardExtraMaterialsPricesGbp = standardExtraMaterialsPricesGbp;
    }

    public Map<String, Integer> getWarrantyYears() {
        return warrantyYears;
    }

    public void setWarrantyYears(Map<String, Integer> warrantyYears) {
        this.warrantyYears = warrantyYears;
    }

    public Map<String, String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(Map<String, String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
