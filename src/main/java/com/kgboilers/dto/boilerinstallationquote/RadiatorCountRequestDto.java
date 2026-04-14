package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RadiatorCountRequestDto {

    @NotNull
    private RadiatorCount radiatorCount;
}
