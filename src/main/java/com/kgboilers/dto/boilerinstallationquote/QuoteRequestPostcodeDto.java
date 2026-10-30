package com.kgboilers.dto.boilerinstallationquote;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class QuoteRequestPostcodeDto {

    @NotBlank(message = "Postcode is required")
    @Size(max = 16, message = "Please enter a valid UK postcode")
    @Pattern(regexp = "(?i)^\\s*(?:GIR\\s*0AA|[A-Z]{1,2}[0-9][A-Z0-9]?\\s*[0-9][A-Z]{2})\\s*$",
            message = "Please enter a valid UK postcode")
    private String postcode;
}