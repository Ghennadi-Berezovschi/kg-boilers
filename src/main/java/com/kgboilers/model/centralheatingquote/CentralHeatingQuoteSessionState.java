package com.kgboilers.model.centralheatingquote;

import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.MagneticFilterStatus;
import com.kgboilers.model.centralheatingquote.enums.PowerFlushStatus;
import com.kgboilers.model.centralheatingquote.enums.TrvValveStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CentralHeatingQuoteSessionState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String postcode;
    private FuelType fuel;
    private OwnershipType ownership;
    private PropertyType propertyType;
    private BoilerType boilerType;
    private Bedrooms bedrooms;
    private RadiatorCount radiatorCount;
    private TrvValveStatus trvValveStatus;
    private PowerFlushStatus powerFlushStatus;
    private MagneticFilterStatus magneticFilterStatus;

    private CentralHeatingQuoteStep currentStep = CentralHeatingQuoteStep.START;

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

    public boolean hasBoilerType() {
        return boilerType != null;
    }

    public boolean hasBedrooms() {
        return bedrooms != null;
    }

    public boolean hasRadiatorCount() {
        return radiatorCount != null;
    }

    public boolean hasTrvValveStatus() {
        return trvValveStatus != null;
    }

    public boolean hasPowerFlushStatus() {
        return powerFlushStatus != null;
    }

    public boolean hasMagneticFilterStatus() {
        return magneticFilterStatus != null;
    }

    public String getRadiatorCountSummary() {
        if (radiatorCount == null) {
            return "";
        }

        return radiatorCount.getValue() + " radiators";
    }

    public String getTrvValveStatusSummary() {
        if (trvValveStatus == null) {
            return "";
        }

        return trvValveStatus.getLabel();
    }
}
