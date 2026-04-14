package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.FlueLengthPricingProperties;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlueLengthPricingServiceTest {

    private final FlueLengthPricingService service = new FlueLengthPricingService(
            new FlueLengthPricingProperties(0, 250, 500, 700, 900)
    );

    @Test
    void getPrice_shouldReturnConfiguredPriceForEachLength() {
        assertEquals(0, service.getPrice(FlueLength.ZERO_TO_ONE));
        assertEquals(250, service.getPrice(FlueLength.TWO_TO_THREE));
        assertEquals(500, service.getPrice(FlueLength.FOUR_TO_FIVE));
        assertEquals(700, service.getPrice(FlueLength.SIX_TO_SEVEN));
        assertEquals(900, service.getPrice(FlueLength.SEVEN_PLUS));
    }
}
