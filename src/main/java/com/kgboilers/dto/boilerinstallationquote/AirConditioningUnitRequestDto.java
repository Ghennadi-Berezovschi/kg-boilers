package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.AirConditioningUnit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AirConditioningUnitRequestDto {

    @NotEmpty(message = "Please choose at least one air conditioning unit")
    @Valid
    private List<Item> airConditioningUnits;

    @Data
    public static class Item {

        @NotNull(message = "Air conditioning unit is required")
        private AirConditioningUnit airConditioningUnit;

        @Min(value = 1, message = "Unit quantity must be at least 1")
        @Max(value = 9, message = "Unit quantity must be 9 or less")
        private int quantity = 1;
    }
}
