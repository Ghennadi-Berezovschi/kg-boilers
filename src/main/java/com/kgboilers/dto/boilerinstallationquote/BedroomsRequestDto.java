package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BedroomsRequestDto {

    @NotNull
    private Bedrooms bedrooms;
}