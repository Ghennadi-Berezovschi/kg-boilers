package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.BoilerCatalogProperties;
import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BoilerCatalogConfigurationTest {

    private BoilerCatalogProperties loadCatalog() {
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("catalog/boilers-combi.yml"),
                new ClassPathResource("catalog/boilers-system.yml"),
                new ClassPathResource("catalog/boilers-heat-only.yml"));
        return new Binder(new MapConfigurationPropertySource(yaml.getObject()))
                .bind("kg.boilers", Bindable.of(BoilerCatalogProperties.class)).get();
    }

    @Test
    void allCatalogEntriesHaveDistinctManufacturerArticleNumbers() {
        var catalog = loadCatalog();
        List<BoilerModel> boilers = Stream.of(catalog.getCombi(), catalog.getSystem(), catalog.getHeatOnly())
                .flatMap(List::stream).toList();

        assertEquals(24, boilers.size());
        assertTrue(boilers.stream().allMatch(b -> b.getManufacturerArticleNumber() != null
                && b.getManufacturerArticleNumber().matches("\\d{7,10}")));
        assertEquals(boilers.size(), boilers.stream().map(BoilerModel::getManufacturerArticleNumber).distinct().count());
    }

    @Test
    void vaillantSystemCatalogUsesCurrentGenerationAndMatchingPowerRatings() {
        var boilers = loadCatalog().getSystem().stream().filter(b -> b.getBrand().equals("Vaillant")).toList();
        var currentRatings = Map.of("0010036009", 10, "0010036010", 15, "0010036011", 20,
                "0010036012", 25, "0010036013", 30);

        assertEquals(currentRatings.keySet(), boilers.stream().map(BoilerModel::getManufacturerArticleNumber)
                .collect(java.util.stream.Collectors.toSet()));
        boilers.forEach(boiler -> assertEquals(currentRatings.get(boiler.getManufacturerArticleNumber()), boiler.getPowerKw()));
        assertTrue(boilers.stream().allMatch(b -> b.getDimensions().equals("W 440mm x D 352mm x H 720mm")));
    }

    @Test
    void actualCatalogRecommendsCurrent620ForMediumSystemDemandWithFlueCountedOnce() {
        var service = new BoilerRecommendationService(new BoilerCatalogService(loadCatalog()));
        var state = new QuoteSessionState();
        state.setBoilerType(BoilerType.SYSTEM);
        state.setRadiatorCount(RadiatorCount.SIX_TO_NINE);
        state.setBathShowerCount(BathShowerCount.TWO);

        var boilers = service.recommend(state).getBoilers();
        var current620 = boilers.stream().filter(b -> "0010036011".equals(b.getManufacturerArticleNumber()))
                .findFirst().orElseThrow();
        assertEquals(20, current620.getPowerKw());
        assertEquals(1650, current620.getAveragePriceGbp());
        assertEquals(2780, current620.getTotalPriceGbp());
        assertTrue(boilers.stream().noneMatch(b -> b.getModel().contains("618") || b.getModel().contains("612")));
    }
}
