package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerCondition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerConditionRequestDto {

    @NotNull(message = "Boiler condition is required")
    private BoilerCondition boilerCondition;
}
