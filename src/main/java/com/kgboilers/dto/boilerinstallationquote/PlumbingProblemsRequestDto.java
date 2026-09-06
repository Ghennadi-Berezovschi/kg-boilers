package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.PlumbingProblem;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PlumbingProblemsRequestDto {

    @NotEmpty(message = "Select at least one plumbing problem")
    private List<PlumbingProblem> problems;
}
