package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.FuelType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FuelRequestDto {
    @NotNull(message = "Fuel type is required")
    private FuelType fuel;
}
