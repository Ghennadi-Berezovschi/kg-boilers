package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.FlueClearance;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlueClearanceRequestDto {

    @NotNull(message = "Flue clearance is required")
    private FlueClearance flueClearance;
}
