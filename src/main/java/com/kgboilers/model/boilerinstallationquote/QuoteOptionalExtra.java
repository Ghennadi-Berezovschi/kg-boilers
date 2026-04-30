package com.kgboilers.model.boilerinstallationquote;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuoteOptionalExtra {

    private String id;
    private String title;
    private String description;
    private Integer priceGbp;
    private String image;
    private boolean repeatable;
    private Integer quantity = 1;
    private List<String> appliesToBoilerTypes = new ArrayList<>();
}
