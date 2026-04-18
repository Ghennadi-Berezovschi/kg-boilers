package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.centralheatingquote.enums.InstallationPositionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InstallationPositionRequestDto {

    @NotNull(message = "Please choose same position, different position or no existing item there")
    private InstallationPositionType installationPositionType;
}
