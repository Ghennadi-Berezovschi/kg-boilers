package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.RelocationPricingProperties;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RelocationPricingService {

    private final RelocationPricingProperties relocationPricingProperties;

    public RelocationPricingService(RelocationPricingProperties relocationPricingProperties) {
        this.relocationPricingProperties = relocationPricingProperties;
    }

    public int getPrice(RelocationDistance relocationDistance) {
        if (relocationDistance == null) {
            return 0;
        }

        return switch (relocationDistance) {
            case ZERO_TO_ONE -> relocationPricingProperties.zeroToOne();
            case TWO_TO_THREE -> relocationPricingProperties.twoToThree();
            case FOUR_TO_FIVE -> relocationPricingProperties.fourToFive();
            case SIX_TO_SEVEN -> relocationPricingProperties.sixToSeven();
            case SEVEN_PLUS -> relocationPricingProperties.sevenPlus();
        };
    }

    public Map<String, Integer> getPricesByValue() {
        return Map.of(
                RelocationDistance.ZERO_TO_ONE.getValue(), relocationPricingProperties.zeroToOne(),
                RelocationDistance.TWO_TO_THREE.getValue(), relocationPricingProperties.twoToThree(),
                RelocationDistance.FOUR_TO_FIVE.getValue(), relocationPricingProperties.fourToFive(),
                RelocationDistance.SIX_TO_SEVEN.getValue(), relocationPricingProperties.sixToSeven(),
                RelocationDistance.SEVEN_PLUS.getValue(), relocationPricingProperties.sevenPlus()
        );
    }
}
