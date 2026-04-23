package com.kgboilers.dto.boilerrepairquote;

import com.kgboilers.model.boilerrepair.enums.MagneticFilterStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MagneticFilterRequestDto {

    @NotNull(message = "Magnetic filter answer is required")
    private MagneticFilterStatus magneticFilterStatus;
}
