package com.kgboilers.dto.centralheatingquote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IssueDetailsRequestDto {

    @NotBlank(message = "Please describe the issue")
    @Size(max = 500, message = "Please keep the problem description under 500 characters")
    private String issueDetails;
}
