package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.PowerFlushStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PowerFlushRequestDto {

    @NotNull(message = "Power flush answer is required")
    private PowerFlushStatus powerFlushStatus;
}
