package com.kgboilers.dto.boilerinstallationquote;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
public class BoilerContactRequestDto {

    @NotBlank(message = "Please choose a boiler first.")
    private String selectedBoiler;

    @NotBlank(message = "Please enter your name.")
    @Size(max = 255, message = "Name is too long.")
    private String name;

    @NotBlank(message = "Please enter your email address.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 255, message = "Email is too long.")
    private String email;

    @NotBlank(message = "Please enter your phone number.")
    @Pattern(
            regexp = "^(?=(?:[^0-9]*[0-9]){7})[0-9+()\\-\\s]{7,20}$",
            message = "Please enter a valid phone number."
    )
    private String phone;

    private List<String> selectedExtras = new ArrayList<>();

    private List<MultipartFile> pictures = new ArrayList<>();
}
