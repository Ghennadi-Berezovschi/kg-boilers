package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RelocationDistanceRequestDto {

    @NotNull(message = "Relocation distance is required")
    private RelocationDistance relocationDistance;
}