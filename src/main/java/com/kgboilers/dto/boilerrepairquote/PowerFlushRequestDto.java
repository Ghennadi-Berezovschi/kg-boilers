package com.kgboilers.dto.boilerrepairquote;

import com.kgboilers.model.boilerrepair.enums.PowerFlushStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PowerFlushRequestDto {

    @NotNull(message = "Power flush answer is required")
    private PowerFlushStatus powerFlushStatus;
}
