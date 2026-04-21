package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerPressureStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerPressureRequestDto {

    @NotNull(message = "Boiler pressure answer is required")
    private BoilerPressureStatus boilerPressureStatus;
}
