package com.kgboilers.dto.centralheatingquote;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class TrvInstallationQuantityRequestDto {

    @Min(value = 0, message = "Please enter a valid valve quantity")
    @Max(value = 99, message = "Maximum quantity is 99")
    private Integer trvValvesQuantity;
    @Min(value = 0, message = "Please enter a valid valve quantity")
    @Max(value = 99, message = "Maximum quantity is 99")
    private Integer lockshieldValvesQuantity;
    @Min(value = 0, message = "Please enter a valid valve quantity")
    @Max(value = 99, message = "Maximum quantity is 99")
    private Integer towelRailValvesQuantity;
}
