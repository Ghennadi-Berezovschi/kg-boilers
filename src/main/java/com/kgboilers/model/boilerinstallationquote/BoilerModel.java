package com.kgboilers.model.boilerinstallationquote;

import lombok.Data;


@Data
public class BoilerModel {

    private String brand;
    private String model;
    private Integer powerKw;
    private Integer radiatorsMin;
    private Integer radiatorsMax;
    private Integer bathroomsMin;
    private Integer bathroomsMax;
    private Integer averagePriceGbp;
    private Integer standardInstallationGbp;
    private StandardItems standardItems = new StandardItems();
    private String image;
    private String dimensions;
    private String hotWaterFlowRate;
    private Integer warrantyYears;
    private boolean enabled;

    public int getStandardItemsTotalGbp() {
        return standardItems == null ? 0 : standardItems.getTotalGbp();
    }

    public int getTotalPriceGbp() {
        return safePrice(averagePriceGbp) + safePrice(standardInstallationGbp) + getStandardItemsTotalGbp();
    }

    private int safePrice(Integer priceGbp) {
        return priceGbp == null ? 0 : priceGbp;
    }

    @Data
    public static class StandardItems {
        private Integer magneticFilterGbp;
        private Integer scaleReducerGbp;
        private Integer roomThermostatGbp;
        private Integer coAlarmGbp;
        private Integer pipesAndFittingsGbp;

        public int getTotalGbp() {
            return safePrice(magneticFilterGbp)
                    + safePrice(scaleReducerGbp)
                    + safePrice(roomThermostatGbp)
                    + safePrice(coAlarmGbp)
                    + safePrice(pipesAndFittingsGbp);
        }

        private int safePrice(Integer priceGbp) {
            return priceGbp == null ? 0 : priceGbp;
        }
    }
}
