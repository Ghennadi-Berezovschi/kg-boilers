package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InstallationPipeDistanceRequestDto {

    @NotNull(message = "Please choose how far the nearest heating pipe is")
    private RelocationDistance pipeDistance;
}
