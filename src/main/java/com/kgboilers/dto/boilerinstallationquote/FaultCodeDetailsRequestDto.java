package com.kgboilers.dto.boilerinstallationquote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FaultCodeDetailsRequestDto {

    @NotBlank(message = "Please provide the fault code or describe the signal shown")
    @Size(max = 500, message = "Please keep the fault code description under 500 characters")
    private String faultCodeDetails;
}
