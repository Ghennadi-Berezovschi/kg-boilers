package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerLocationRequestDto {

    @NotNull
    private BoilerLocation location;
}
