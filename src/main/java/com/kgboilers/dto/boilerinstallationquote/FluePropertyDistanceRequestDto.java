package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.FluePropertyDistance;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FluePropertyDistanceRequestDto {

    @NotNull(message = "Flue property distance is required")
    private FluePropertyDistance fluePropertyDistance;
}
