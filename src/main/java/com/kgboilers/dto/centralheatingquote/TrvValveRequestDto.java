package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.centralheatingquote.enums.TrvValveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrvValveRequestDto {

    @NotNull
    private TrvValveStatus trvValveStatus;
}
