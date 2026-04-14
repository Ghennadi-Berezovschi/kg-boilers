package com.kgboilers.dto.boilerinstallationquote;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class QuoteRequestPostcodeDto {

    @NotBlank(message = "Postcode is required")
    private String postcode;
}