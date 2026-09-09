package com.kgboilers.model.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.AirConditioningRoomSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirConditioningRoomSizeSelection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private AirConditioningRoomSize roomSize;
    private int quantity;

    public String getSummary() {
        if (roomSize == null) {
            return "";
        }

        return quantity > 1
                ? roomSize.getLabel() + " x" + quantity
                : roomSize.getLabel();
    }
}
