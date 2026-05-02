package com.kgboilers.dto.boilerinstallationquote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProblemDetailsRequestDto {

    @NotBlank(message = "Problem details are required")
    @Size(max = 500, message = "Please keep the problem description under 500 characters")
    private String problemDetails;
}
