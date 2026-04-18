package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.centralheatingquote.enums.RadiatorConvectorType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RadiatorSpecificationRequestDto {

    private RadiatorConvectorType radiatorConvectorType;
    @Min(value = 1, message = "Please enter the radiator length in mm")
    @Max(value = 2000, message = "Maximum length is 2000 mm")
    private Integer radiatorLengthMm;
    @Min(value = 1, message = "Please enter the radiator width in mm")
    @Max(value = 2000, message = "Maximum width is 2000 mm")
    private Integer radiatorWidthMm;
    @Min(value = 1, message = "Please enter the towel rail length in mm")
    @Max(value = 2000, message = "Maximum length is 2000 mm")
    private Integer towelRailLengthMm;
    @Min(value = 1, message = "Please enter the towel rail width in mm")
    @Max(value = 2000, message = "Maximum width is 2000 mm")
    private Integer towelRailWidthMm;
    @Min(value = 1, message = "Please enter the quantity")
    @Max(value = 99, message = "Maximum quantity is 99")
    private Integer installationQuantity;
}
