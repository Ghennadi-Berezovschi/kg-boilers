package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerPosition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerPositionRequestDto {

    @NotNull(message = "Boiler position is required")
    private BoilerPosition boilerPosition;
}