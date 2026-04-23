package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.HorizontalFlueShape;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HorizontalFlueShapeRequestDto {

    @NotNull(message = "Flue shape is required")
    private HorizontalFlueShape flueShape;
}
