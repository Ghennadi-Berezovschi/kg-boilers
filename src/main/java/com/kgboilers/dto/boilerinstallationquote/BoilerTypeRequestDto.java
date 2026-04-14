package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerTypeRequestDto {

    @NotNull(message = "Boiler type is required")
    private BoilerType boilerType;
}
