package com.kgboilers.model.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.GasApplianceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasApplianceSelection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private GasApplianceType appliance;
    private int quantity;

    public String getSummary() {
        if (appliance == null) {
            return "";
        }

        return quantity > 1
                ? appliance.getLabel() + " x" + quantity
                : appliance.getLabel();
    }
}
