package com.kgboilers.model.centralheatingquote;

import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.centralheatingquote.enums.InstallationItemType;
import com.kgboilers.model.centralheatingquote.enums.InstallationPositionType;
import com.kgboilers.model.centralheatingquote.enums.RadiatorConvectorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CentralHeatingInstallationItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private InstallationItemType installationItemType;
    private InstallationPositionType installationPositionType;
    private RelocationDistance moveDistance;
    private RelocationDistance nearestPipeDistance;
    private RadiatorConvectorType radiatorConvectorType;
    private Integer lengthMm;
    private Integer widthMm;
    private Integer quantity;

    public String getSummary() {
        String positionSummary = switch (installationPositionType) {
            case DIFFERENT_POSITION -> installationPositionType.getLabel()
                    + (moveDistance != null ? " (" + moveDistance.getValue() + " metres)" : "");
            case NO_EXISTING_ITEM -> installationPositionType.getLabel()
                    + (nearestPipeDistance != null ? " (nearest pipe " + nearestPipeDistance.getValue() + " metres)" : "");
            default -> installationPositionType.getLabel();
        };

        if (installationItemType == InstallationItemType.RADIATOR) {
            return "Radiator: "
                    + positionSummary
                    + ", "
                    + radiatorConvectorType.getLabel()
                    + ", "
                    + lengthMm
                    + "mm x "
                    + widthMm
                    + "mm, qty "
                    + quantity;
        }

        return "Towel rail: "
                + positionSummary
                + ", "
                + lengthMm
                + "mm x "
                + widthMm
                + "mm, qty "
                + quantity;
    }
}
