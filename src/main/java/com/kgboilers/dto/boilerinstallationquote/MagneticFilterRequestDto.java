package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.MagneticFilterStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MagneticFilterRequestDto {

    @NotNull(message = "Magnetic filter answer is required")
    private MagneticFilterStatus magneticFilterStatus;
}
