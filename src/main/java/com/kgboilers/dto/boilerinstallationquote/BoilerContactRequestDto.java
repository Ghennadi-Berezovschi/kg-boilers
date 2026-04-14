package com.kgboilers.dto.boilerinstallationquote;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BoilerContactRequestDto {

    @NotBlank(message = "Please choose a boiler first.")
    private String selectedBoiler;

    @NotBlank(message = "Please enter your email address.")
    @Email(message = "Please enter a valid email address.")
    private String email;

    @NotBlank(message = "Please enter your phone number.")
    @Pattern(
            regexp = "^[0-9+()\\-\\s]{7,20}$",
            message = "Please enter a valid phone number."
    )
    private String phone;
}
