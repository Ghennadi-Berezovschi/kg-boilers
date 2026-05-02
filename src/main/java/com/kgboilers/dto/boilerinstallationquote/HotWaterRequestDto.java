package com.kgboilers.dto.boilerinstallationquote;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HotWaterRequestDto {

    @NotNull(message = "Hot water answer is required")
    private Boolean hotWaterAvailable;
}
