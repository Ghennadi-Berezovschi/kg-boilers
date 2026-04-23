package com.kgboilers.dto.boilerrepairquote;

import com.kgboilers.model.boilerrepair.enums.BoilerAge;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerAgeRequestDto {

    @NotNull(message = "Boiler age is required")
    private BoilerAge boilerAge;
}
