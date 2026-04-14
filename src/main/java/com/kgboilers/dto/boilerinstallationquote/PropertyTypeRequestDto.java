package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PropertyTypeRequestDto {

    @NotNull
    private PropertyType propertyType;
}
