package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OwnershipRequestDto {
    @NotNull(message = "Ownership type is required")
    private OwnershipType ownership;
}
