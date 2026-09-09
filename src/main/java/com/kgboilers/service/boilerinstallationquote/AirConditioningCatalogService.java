package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.AirConditioningCatalogProperties;
import com.kgboilers.model.boilerinstallation.enums.AirConditioningUnit;
import com.kgboilers.model.boilerinstallationquote.AirConditioningCatalogItem;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AirConditioningCatalogService {

    private final AirConditioningCatalogProperties properties;

    public AirConditioningCatalogService(AirConditioningCatalogProperties properties) {
        this.properties = properties;
    }

    public List<AirConditioningCatalogItem> getCatalogItems() {
        return Arrays.stream(AirConditioningUnit.values())
                .map(unit -> new AirConditioningCatalogItem(
                        unit,
                        getPurchasePriceGbp(unit),
                        getStandardInstallationWorksPriceGbp(unit),
                        getStandardExtraMaterialsPriceGbp(unit),
                        getWarrantyYears(unit),
                        getImageUrl(unit)
                ))
                .toList();
    }

    public int getPurchasePriceGbp(AirConditioningUnit unit) {
        if (unit == null) {
            return 0;
        }

        return properties.getPurchasePricesGbp().getOrDefault(unit.getValue(), 0);
    }

    public int getStandardInstallationWorksPriceGbp(AirConditioningUnit unit) {
        if (unit == null) {
            return 0;
        }

        return properties.getStandardInstallationWorksPricesGbp().getOrDefault(unit.getValue(), 0);
    }

    public int getStandardExtraMaterialsPriceGbp(AirConditioningUnit unit) {
        if (unit == null) {
            return 0;
        }

        return properties.getStandardExtraMaterialsPricesGbp().getOrDefault(unit.getValue(), 0);
    }

    public int getWarrantyYears(AirConditioningUnit unit) {
        if (unit == null) {
            return 0;
        }

        return properties.getWarrantyYears().getOrDefault(unit.getValue(), 0);
    }

    public String getImageUrl(AirConditioningUnit unit) {
        if (unit == null) {
            return "";
        }

        return properties.getImageUrls().getOrDefault(unit.getValue(), "");
    }
}
