package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.FaultCodeDisplayStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FaultCodeDisplayRequestDto {

    @NotNull(message = "Fault code answer is required")
    private FaultCodeDisplayStatus faultCodeDisplayStatus;
}
