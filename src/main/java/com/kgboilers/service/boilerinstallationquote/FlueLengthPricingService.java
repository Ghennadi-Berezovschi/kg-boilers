package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.FlueLengthPricingProperties;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FlueLengthPricingService {

    private final FlueLengthPricingProperties flueLengthPricingProperties;

    public FlueLengthPricingService(FlueLengthPricingProperties flueLengthPricingProperties) {
        this.flueLengthPricingProperties = flueLengthPricingProperties;
    }

    public int getPrice(FlueLength flueLength) {
        if (flueLength == null) {
            return 0;
        }

        return switch (flueLength) {
            case ZERO_TO_ONE -> flueLengthPricingProperties.zeroToOne();
            case TWO_TO_THREE -> flueLengthPricingProperties.twoToThree();
            case FOUR_TO_FIVE -> flueLengthPricingProperties.fourToFive();
            case SIX_TO_SEVEN -> flueLengthPricingProperties.sixToSeven();
            case SEVEN_PLUS -> flueLengthPricingProperties.sevenPlus();
        };
    }

    public Map<String, Integer> getPricesByValue() {
        return Map.of(
                FlueLength.ZERO_TO_ONE.getValue(), flueLengthPricingProperties.zeroToOne(),
                FlueLength.TWO_TO_THREE.getValue(), flueLengthPricingProperties.twoToThree(),
                FlueLength.FOUR_TO_FIVE.getValue(), flueLengthPricingProperties.fourToFive(),
                FlueLength.SIX_TO_SEVEN.getValue(), flueLengthPricingProperties.sixToSeven(),
                FlueLength.SEVEN_PLUS.getValue(), flueLengthPricingProperties.sevenPlus()
        );
    }
}
