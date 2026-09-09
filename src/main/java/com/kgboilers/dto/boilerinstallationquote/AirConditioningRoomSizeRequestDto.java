package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.AirConditioningRoomSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AirConditioningRoomSizeRequestDto {

    @NotEmpty(message = "Please choose at least one room size")
    @Valid
    private List<Item> roomSizes;

    @Data
    public static class Item {

        @NotNull(message = "Room size is required")
        private AirConditioningRoomSize roomSize;

        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 9, message = "Quantity cannot be more than 9")
        private int quantity = 1;
    }
}
