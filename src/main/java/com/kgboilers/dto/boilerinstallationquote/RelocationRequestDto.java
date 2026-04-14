package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.Relocation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RelocationRequestDto {

    @NotNull(message = "Relocation is required")
    private Relocation relocation;
}