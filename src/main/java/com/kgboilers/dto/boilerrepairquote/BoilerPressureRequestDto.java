package com.kgboilers.dto.boilerrepairquote;

import com.kgboilers.model.boilerrepair.enums.BoilerPressureStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerPressureRequestDto {

    @NotNull(message = "Boiler pressure answer is required")
    private BoilerPressureStatus boilerPressureStatus;
}
