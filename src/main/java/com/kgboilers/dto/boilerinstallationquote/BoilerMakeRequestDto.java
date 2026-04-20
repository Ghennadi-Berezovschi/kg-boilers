package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerMakeRequestDto {

    @NotNull(message = "Boiler make is required")
    private BoilerMake boilerMake;
}
