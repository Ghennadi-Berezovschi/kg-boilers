package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.FlueClearancePricingProperties;
import com.kgboilers.model.boilerinstallation.enums.FlueClearance;
import org.springframework.stereotype.Service;

@Service
public class FlueClearancePricingService {

    private final FlueClearancePricingProperties flueClearancePricingProperties;

    public FlueClearancePricingService(FlueClearancePricingProperties flueClearancePricingProperties) {
        this.flueClearancePricingProperties = flueClearancePricingProperties;
    }

    public int getPrice(FlueClearance flueClearance) {
        if (flueClearance == null) {
            return 0;
        }

        return switch (flueClearance) {
            case THIRTY_CM_OR_MORE -> flueClearancePricingProperties.thirtyCmOrMore();
            case LESS_THAN_THIRTY_CM -> flueClearancePricingProperties.lessThanThirtyCm();
            case UNSURE -> flueClearancePricingProperties.unsure();
        };
    }
}
