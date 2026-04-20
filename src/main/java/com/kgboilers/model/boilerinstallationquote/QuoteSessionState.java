package com.kgboilers.model.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.BoilerCondition;
import com.kgboilers.model.boilerinstallation.enums.BoilerFloorLevel;
import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.BoilerPosition;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.FlueClearance;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import com.kgboilers.model.boilerinstallation.enums.FluePropertyDistance;
import com.kgboilers.model.boilerinstallation.enums.FluePosition;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.HeatOnlyConversion;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.boilerinstallation.enums.SlopedRoofPosition;
import com.kgboilers.model.boilerinstallation.enums.VerticalFlueType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class QuoteSessionState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // =========================
    // DATA
    // =========================

    private String postcode;
    private FuelType fuel;
    private OwnershipType ownership;
    private PropertyType propertyType;
    private Bedrooms bedrooms;

    private BoilerType boilerType;
    private BoilerMake boilerMake;
    private HeatOnlyConversion heatOnlyConversion;
    private BoilerPosition boilerPosition;
    private BoilerLocation boilerLocation;
    private BoilerFloorLevel boilerFloorLevel;
    private BoilerCondition boilerCondition;

    private Relocation relocation;
    private RelocationDistance relocationDistance;
    private FlueType flueType;
    private VerticalFlueType verticalFlueType;
    private FlueLength flueLength;
    private SlopedRoofPosition slopedRoofPosition;
    private FluePosition fluePosition;
    private FlueClearance flueClearance;
    private FluePropertyDistance fluePropertyDistance;
    private RadiatorCount radiatorCount;
    private BathShowerCount bathShowerCount;

    private QuoteStep currentStep = QuoteStep.START;

    // =========================
    // STEP CHECKS
    // =========================

    public boolean hasPostcode() {
        return postcode != null && !postcode.isBlank();
    }

    public boolean hasFuel() {
        return fuel != null;
    }

    public boolean hasOwnership() {
        return ownership != null;
    }

    public boolean hasPropertyType() {
        return propertyType != null;
    }

    public boolean hasBedrooms() {
        return bedrooms != null;
    }

    public boolean hasBoilerType() {
        return boilerType != null;
    }

    public boolean hasBoilerMake() {
        return boilerMake != null;
    }

    public boolean hasHeatOnlyConversion() {
        return heatOnlyConversion != null;
    }

    public boolean hasBoilerPosition() {
        return boilerPosition != null;
    }

    public boolean hasBoilerLocation() {
        return boilerLocation != null;
    }

    public boolean hasBoilerFloorLevel() {
        return boilerFloorLevel != null;
    }

    public boolean hasBoilerCondition() {
        return boilerCondition != null;
    }

    public boolean hasRelocation() {
        return relocation != null;
    }

    public boolean hasRelocationDistance() {
        return relocationDistance != null;
    }

    public boolean hasFlueType() {
        return flueType != null;
    }

    public boolean hasVerticalFlueType() {
        return verticalFlueType != null;
    }

    public boolean hasCompleteFlueSelection() {
        if (flueType == null) {
            return false;
        }

        return flueType != FlueType.VERTICAL || verticalFlueType != null;
    }

    public boolean hasFlueLength() {
        return flueLength != null;
    }

    public boolean hasSlopedRoofPosition() {
        return slopedRoofPosition != null;
    }

    public boolean hasFluePosition() {
        return fluePosition != null;
    }

    public boolean hasFlueClearance() {
        return flueClearance != null;
    }

    public boolean hasFluePropertyDistance() {
        return fluePropertyDistance != null;
    }

    public boolean hasRadiatorCount() {
        return radiatorCount != null;
    }

    public boolean hasBathShowerCount() {
        return bathShowerCount != null;
    }

    public String getFlueSummary() {
        if (flueType == null) {
            return "";
        }

        if (flueType == FlueType.HORIZONTAL) {
            return "Horizontal flue";
        }

        if (verticalFlueType == null) {
            return "Vertical flue";
        }

        return "Vertical flue (" + verticalFlueType.getDisplayName() + ")";
    }

    public String getFlueLengthSummary() {
        if (flueLength == null) {
            return "";
        }

        return flueLength.getValue() + " metres";
    }

    public String getSlopedRoofPositionSummary() {
        if (slopedRoofPosition == null) {
            return "";
        }

        return slopedRoofPosition.getDisplayName();
    }

    public String getFluePositionSummary() {
        if (fluePosition == null) {
            return "";
        }

        return switch (fluePosition) {
            case UNDER_STRUCTURE -> "Under balcony or structure";
            case OPEN_AREA -> "Open area";
        };
    }

    public String getFlueClearanceSummary() {
        if (flueClearance == null) {
            return "";
        }

        return switch (flueClearance) {
            case THIRTY_CM_OR_MORE -> "30 cm or more from an opening";
            case LESS_THAN_THIRTY_CM -> "Less than 30 cm from an opening";
            case UNSURE -> "Unsure";
        };
    }

    public String getRelocationDistanceSummary() {
        if (relocationDistance == null) {
            return "";
        }

        return relocationDistance.getValue() + " metres";
    }

    public String getFluePropertyDistanceSummary() {
        if (fluePropertyDistance == null) {
            return "";
        }

        return switch (fluePropertyDistance) {
            case LESS_THAN_ONE_METRE -> "Less than 1 metre";
            case MORE_THAN_ONE_METRE -> "More than 1 metre";
        };
    }

    public String getRadiatorCountSummary() {
        if (radiatorCount == null) {
            return "";
        }

        return radiatorCount.getValue() + " radiators";
    }

    public String getBathShowerCountSummary() {
        if (bathShowerCount == null) {
            return "";
        }

        return bathShowerCount.getValue();
    }

    // =========================
    // FINAL STATE (FIXED LOGIC)
    // =========================

    public boolean isComplete() {
        return hasPostcode()
                && hasFuel()
                && hasOwnership()
                && hasPropertyType()
                && hasBedrooms()
                && hasBoilerType()
                && hasBoilerPosition()
                && hasBoilerLocation()
                && hasBoilerFloorLevel()
                && hasBoilerCondition()
                && hasRelocation()
                && (relocation == Relocation.NO || hasRelocationDistance())
                && hasCompleteFlueSelection()
                && hasFlueLength()
                && (verticalFlueType != VerticalFlueType.SLOPED_ROOF || hasSlopedRoofPosition())
                && (flueType != FlueType.HORIZONTAL || hasFluePosition())
                && hasBathShowerCount()
                && hasRadiatorCount()
                && (flueType != FlueType.HORIZONTAL
                || (hasFlueClearance() && hasFluePropertyDistance()));
    }
}
