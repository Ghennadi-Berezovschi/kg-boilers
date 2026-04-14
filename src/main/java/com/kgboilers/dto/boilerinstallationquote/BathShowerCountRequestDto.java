package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BathShowerCountRequestDto {

    @NotNull
    private BathShowerCount bathShowerCount;
}
