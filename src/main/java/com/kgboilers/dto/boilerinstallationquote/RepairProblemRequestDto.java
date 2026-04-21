package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.RepairProblem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairProblemRequestDto {

    @NotNull(message = "Please choose what is not working")
    private RepairProblem repairProblem;
}
