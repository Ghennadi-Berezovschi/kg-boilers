package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.FluePosition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FluePositionRequestDto {

    @NotNull(message = "Flue position is required")
    private FluePosition fluePosition;
}