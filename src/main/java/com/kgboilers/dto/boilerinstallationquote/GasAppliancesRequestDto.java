package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.GasApplianceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GasAppliancesRequestDto {

    @NotEmpty(message = "Select at least one gas appliance")
    @Valid
    private List<Item> appliances;

    @Data
    public static class Item {

        @NotNull(message = "Gas appliance type is required")
        private GasApplianceType appliance;

        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 9, message = "Quantity cannot be more than 9")
        private int quantity = 1;
    }
}
