package com.kgboilers.dto.boilerrepairquote;

import com.kgboilers.model.boilerrepair.enums.FaultCodeDisplayStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FaultCodeDisplayRequestDto {

    @NotNull(message = "Fault code answer is required")
    private FaultCodeDisplayStatus faultCodeDisplayStatus;
}
