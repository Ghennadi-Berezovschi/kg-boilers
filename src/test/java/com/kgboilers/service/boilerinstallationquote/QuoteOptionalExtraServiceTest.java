package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.QuoteOfferProperties;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class QuoteOptionalExtraServiceTest {

    @Test
    void getOptionalExtrasFor_shouldKeepDefaultExtrasAwayFromHeatOnly() {
        QuoteOptionalExtra defaultExtra = optionalExtra("hive-thermostat-mini");
        QuoteOptionalExtra heatOnlyExtra = optionalExtra("heat-only-motorised-valves");
        heatOnlyExtra.setAppliesToBoilerTypes(List.of("heat-only"));

        QuoteOptionalExtraService service = service(defaultExtra, heatOnlyExtra);

        assertEquals(List.of(defaultExtra), service.getOptionalExtrasFor(BoilerType.COMBI));
        assertEquals(List.of(heatOnlyExtra), service.getOptionalExtrasFor(BoilerType.HEAT_ONLY));
    }

    @Test
    void resolveSelectedExtras_shouldIgnoreExtrasForOtherBoilerTypes() {
        QuoteOptionalExtra defaultExtra = optionalExtra("hive-thermostat-mini");
        QuoteOptionalExtra heatOnlyExtra = optionalExtra("heat-only-motorised-valves");
        heatOnlyExtra.setAppliesToBoilerTypes(List.of("heat-only"));

        QuoteOptionalExtraService service = service(defaultExtra, heatOnlyExtra);

        assertEquals(List.of(defaultExtra), service.resolveSelectedExtras(List.of(defaultExtra.getId(), heatOnlyExtra.getId()), BoilerType.COMBI));
        assertEquals(List.of(heatOnlyExtra), service.resolveSelectedExtras(List.of(defaultExtra.getId(), heatOnlyExtra.getId()), BoilerType.HEAT_ONLY));
    }

    @Test
    void resolveSelectedExtras_shouldApplyQuantityForRepeatableExtras() {
        QuoteOptionalExtra valve = optionalExtra("heat-only-2-port-position-valve");
        valve.setPriceGbp(99);
        valve.setRepeatable(true);
        valve.setAppliesToBoilerTypes(List.of("heat-only"));

        QuoteOptionalExtraService service = service(valve);

        List<QuoteOptionalExtra> selectedExtras = service.resolveSelectedExtras(
                List.of(valve.getId(), valve.getId(), valve.getId()),
                BoilerType.HEAT_ONLY
        );

        assertEquals(1, selectedExtras.size());
        assertNotSame(valve, selectedExtras.get(0));
        assertEquals(3, selectedExtras.get(0).getQuantity());
        assertEquals(297, selectedExtras.get(0).getPriceGbp());
        assertEquals(297, service.getTotalPriceGbp(selectedExtras));
    }

    @Test
    void resolveSelectedExtras_shouldLimitRepeatableExtrasToNine() {
        QuoteOptionalExtra valve = optionalExtra("heat-only-2-port-position-valve");
        valve.setPriceGbp(99);
        valve.setRepeatable(true);
        valve.setAppliesToBoilerTypes(List.of("heat-only"));

        QuoteOptionalExtraService service = service(valve);

        List<QuoteOptionalExtra> selectedExtras = service.resolveSelectedExtras(
                List.of(
                        valve.getId(), valve.getId(), valve.getId(),
                        valve.getId(), valve.getId(), valve.getId(),
                        valve.getId(), valve.getId(), valve.getId(),
                        valve.getId()
                ),
                BoilerType.HEAT_ONLY
        );

        assertEquals(1, selectedExtras.size());
        assertEquals(9, selectedExtras.get(0).getQuantity());
        assertEquals(891, selectedExtras.get(0).getPriceGbp());
    }

    private QuoteOptionalExtraService service(QuoteOptionalExtra... extras) {
        QuoteOfferProperties properties = new QuoteOfferProperties();
        properties.setOptionalExtras(List.of(extras));
        return new QuoteOptionalExtraService(properties);
    }

    private QuoteOptionalExtra optionalExtra(String id) {
        QuoteOptionalExtra extra = new QuoteOptionalExtra();
        extra.setId(id);
        return extra;
    }
}
