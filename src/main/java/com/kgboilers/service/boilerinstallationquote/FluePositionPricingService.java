package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.FluePositionPricingProperties;
import com.kgboilers.model.boilerinstallation.enums.FluePosition;
import org.springframework.stereotype.Service;

@Service
public class FluePositionPricingService {

    private final FluePositionPricingProperties fluePositionPricingProperties;

    public FluePositionPricingService(FluePositionPricingProperties fluePositionPricingProperties) {
        this.fluePositionPricingProperties = fluePositionPricingProperties;
    }

    public int getPrice(FluePosition fluePosition) {
        if (fluePosition == null) {
            return 0;
        }

        return switch (fluePosition) {
            case UNDER_STRUCTURE -> fluePositionPricingProperties.underStructure();
            case OPEN_AREA -> fluePositionPricingProperties.openArea();
        };
    }
}
