package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.centralheatingquote.enums.MagneticFilterStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MagneticFilterRequestDto {

    @NotNull(message = "Magnetic filter answer is required")
    private MagneticFilterStatus magneticFilterStatus;
}
