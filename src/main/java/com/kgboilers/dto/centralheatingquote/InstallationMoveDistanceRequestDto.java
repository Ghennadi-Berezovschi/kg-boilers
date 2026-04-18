package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InstallationMoveDistanceRequestDto {

    @NotNull(message = "Please choose how far you want to move it")
    private RelocationDistance moveDistance;
}
