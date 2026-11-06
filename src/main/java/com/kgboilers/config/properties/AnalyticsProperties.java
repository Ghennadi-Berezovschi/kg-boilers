package com.kgboilers.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kg.analytics")
public class AnalyticsProperties {
    private String measurementId = "";

    public String getMeasurementId() { return measurementId; }

    public void setMeasurementId(String measurementId) {
        this.measurementId = measurementId == null ? "" : measurementId.trim();
    }

    public boolean isEnabled() {
        return measurementId.matches("G-[A-Z0-9]{6,20}");
    }
}
