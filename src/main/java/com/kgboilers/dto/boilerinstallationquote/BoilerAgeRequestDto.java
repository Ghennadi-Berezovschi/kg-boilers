package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerAge;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerAgeRequestDto {

    @NotNull(message = "Boiler age is required")
    private BoilerAge boilerAge;
}
