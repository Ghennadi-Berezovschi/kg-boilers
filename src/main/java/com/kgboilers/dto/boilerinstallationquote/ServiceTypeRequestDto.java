package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.GasSafetyServiceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceTypeRequestDto {

    @NotNull
    private GasSafetyServiceType serviceType;
}
