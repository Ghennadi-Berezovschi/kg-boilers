package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.centralheatingquote.enums.PowerFlushStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PowerFlushRequestDto {

    @NotNull(message = "Power flush answer is required")
    private PowerFlushStatus powerFlushStatus;
}
