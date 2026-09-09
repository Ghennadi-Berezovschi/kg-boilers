package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.AirConditioningInstallationType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AirConditioningTypeRequestDto {

    @NotEmpty(message = "Please choose at least one air conditioning installation type")
    private List<AirConditioningInstallationType> airConditioningInstallationTypes;
}
