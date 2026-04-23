package com.kgboilers.dto.boilerrepairquote;

import com.kgboilers.model.boilerrepair.enums.RepairProblem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairProblemRequestDto {

    @NotNull(message = "Please choose what is not working")
    private RepairProblem repairProblem;
}
