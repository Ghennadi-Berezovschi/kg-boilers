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
    private String image;
    private String dimensions;
    private String hotWaterFlowRate;
    private Integer warrantyYears;
    private boolean enabled;


}
