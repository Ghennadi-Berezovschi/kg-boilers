package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.RelocationPricingProperties;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelocationPricingServiceTest {

    private final RelocationPricingService service = new RelocationPricingService(
            new RelocationPricingProperties(0, 250, 500, 700, 900)
    );

    @Test
    void getPrice_shouldReturnConfiguredPriceForEachDistance() {
        assertEquals(0, service.getPrice(RelocationDistance.ZERO_TO_ONE));
        assertEquals(250, service.getPrice(RelocationDistance.TWO_TO_THREE));
        assertEquals(500, service.getPrice(RelocationDistance.FOUR_TO_FIVE));
        assertEquals(700, service.getPrice(RelocationDistance.SIX_TO_SEVEN));
        assertEquals(900, service.getPrice(RelocationDistance.SEVEN_PLUS));
    }
}
