package com.kgboilers.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kg.boiler-repair")
public class BoilerRepairProperties {

    private int visitPriceGbp = 79;

    public int getVisitPriceGbp() {
        return visitPriceGbp;
    }

    public void setVisitPriceGbp(int visitPriceGbp) {
        this.visitPriceGbp = visitPriceGbp;
    }
}
