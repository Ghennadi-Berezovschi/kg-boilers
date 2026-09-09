package com.kgboilers.model.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.AirConditioningUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirConditioningUnitSelection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private AirConditioningUnit unit;
    private int quantity;
    private int purchasePriceGbp;
    private int standardInstallationWorksPriceGbp;
    private int standardMaterialsPriceGbp;
    private int warrantyYears;

    public AirConditioningUnitSelection(AirConditioningUnit unit, int quantity) {
        this(unit, quantity, 0, 0, 0, 0);
    }

    public AirConditioningUnitSelection(AirConditioningUnit unit,
                                        int quantity,
                                        int purchasePriceGbp,
                                        int standardInstallationWorksPriceGbp,
                                        int standardMaterialsPriceGbp) {
        this(unit, quantity, purchasePriceGbp, standardInstallationWorksPriceGbp, standardMaterialsPriceGbp, 0);
    }

    public String getSummary() {
        if (unit == null) {
            return "";
        }

        String priceSummary = buildPriceSummary();
        String summary = unit.getLabel() + " ("
                + unit.getCoolingCapacity()
                + ", "
                + unit.getRoomCoverage()
                + priceSummary
                + ")";

        return quantity > 1 ? summary + " x" + quantity : summary;
    }

    private String buildPriceSummary() {
        StringBuilder summary = new StringBuilder();
        appendPrice(summary, "unit", purchasePriceGbp);
        appendPrice(summary, "Standard Installation Works", standardInstallationWorksPriceGbp);
        appendPrice(summary, "Standard Materials", standardMaterialsPriceGbp);
        if (warrantyYears > 0) {
            summary.append(", Warranty ").append(warrantyYears).append(" years");
        }
        return summary.toString();
    }

    private void appendPrice(StringBuilder summary, String label, int priceGbp) {
        if (priceGbp <= 0) {
            return;
        }

        summary.append(", ").append(label).append(" £").append(priceGbp);
    }
}
