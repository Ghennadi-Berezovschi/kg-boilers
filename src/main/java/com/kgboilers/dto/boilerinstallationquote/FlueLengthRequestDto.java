package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlueLengthRequestDto {

    @NotNull(message = "Flue length is required")
    private FlueLength flueLength;
}
