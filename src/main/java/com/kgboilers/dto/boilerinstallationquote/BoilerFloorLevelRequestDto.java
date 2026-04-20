package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BoilerFloorLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoilerFloorLevelRequestDto {

    @NotNull
    private BoilerFloorLevel floorLevel;
}
