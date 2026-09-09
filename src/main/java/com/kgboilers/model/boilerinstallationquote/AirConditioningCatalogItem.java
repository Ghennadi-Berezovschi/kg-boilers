package com.kgboilers.model.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.AirConditioningUnit;

public record AirConditioningCatalogItem(
        AirConditioningUnit unit,
        int purchasePriceGbp,
        int standardInstallationWorksPriceGbp,
        int standardExtraMaterialsPriceGbp,
        int warrantyYears,
        String imageUrl
) {

    public String getValue() {
        return unit.getValue();
    }

    public String getBrand() {
        return unit.getBrand();
    }

    public String getModel() {
        return unit.getModel();
    }

    public String getCoolingCapacity() {
        return unit.getCoolingCapacity();
    }

    public String getRoomCoverage() {
        return unit.getRoomCoverage();
    }
}
