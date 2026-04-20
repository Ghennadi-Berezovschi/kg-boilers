package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.SlopedRoofPosition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SlopedRoofPositionRequestDto {

    @NotNull(message = "Roof position is required")
    private SlopedRoofPosition roofPosition;
}
