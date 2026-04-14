package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.HeatOnlyConversion;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerConversionRequestDto {

    @NotNull(message = "Conversion choice is required")
    private HeatOnlyConversion conversion;
}
