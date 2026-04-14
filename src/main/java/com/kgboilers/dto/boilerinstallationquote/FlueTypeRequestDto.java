package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.VerticalFlueType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlueTypeRequestDto {

    @NotNull(message = "Flue type is required")
    private FlueType flueType;

    private VerticalFlueType verticalFlueType;
}
