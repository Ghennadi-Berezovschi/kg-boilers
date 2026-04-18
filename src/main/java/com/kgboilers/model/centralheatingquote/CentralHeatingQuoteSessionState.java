package com.kgboilers.model.centralheatingquote;

import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.InstallationItemType;
import com.kgboilers.model.centralheatingquote.enums.InstallationPositionType;
import com.kgboilers.model.centralheatingquote.enums.MagneticFilterStatus;
import com.kgboilers.model.centralheatingquote.enums.PowerFlushStatus;
import com.kgboilers.model.centralheatingquote.enums.RadiatorConvectorType;
import com.kgboilers.model.centralheatingquote.enums.RadiatorIssueType;
import com.kgboilers.model.centralheatingquote.enums.TrvValveStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Set<RadiatorIssueType> radiatorIssues = new LinkedHashSet<>();
    private String otherRadiatorIssueDetails;
    private Integer trvValvesQuantity;
    private Integer lockshieldValvesQuantity;
    private Integer towelRailValvesQuantity;
    private List<CentralHeatingInstallationItem> installationItems = new ArrayList<>();
    private InstallationItemType installationItemType;
    private InstallationPositionType installationPositionType;
    private RelocationDistance installationMoveDistance;
    private RelocationDistance installationPipeDistance;
    private RadiatorConvectorType radiatorConvectorType;
    private Integer radiatorLengthMm;
    private Integer radiatorWidthMm;
    private Integer towelRailLengthMm;
    private Integer towelRailWidthMm;
    private Integer installationQuantity;

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

    public boolean hasRadiatorIssues() {
        return radiatorIssues != null && !radiatorIssues.isEmpty();
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

    public String getRadiatorIssuesSummary() {
        if (!hasRadiatorIssues()) {
            return "";
        }

        return radiatorIssues.stream()
                .map(RadiatorIssueType::getLabel)
                .collect(Collectors.joining(", "));
    }

    public boolean needsTrvInstallationQuantity() {
        if (!hasRadiatorIssues()) {
            return false;
        }

        return radiatorIssues.contains(RadiatorIssueType.INSTALL_TRV_VALVES);
    }

    public boolean hasTrvInstallationQuantity() {
        return (trvValvesQuantity != null && trvValvesQuantity > 0)
                || (lockshieldValvesQuantity != null && lockshieldValvesQuantity > 0)
                || (towelRailValvesQuantity != null && towelRailValvesQuantity > 0);
    }

    public String getTrvInstallationQuantitySummary() {
        List<String> parts = new ArrayList<>();

        if (trvValvesQuantity != null && trvValvesQuantity > 0) {
            parts.add("TRV valves: " + trvValvesQuantity);
        }

        if (lockshieldValvesQuantity != null && lockshieldValvesQuantity > 0) {
            parts.add("Lockshield valves: " + lockshieldValvesQuantity);
        }

        if (towelRailValvesQuantity != null && towelRailValvesQuantity > 0) {
            parts.add("Towel rail valves: " + towelRailValvesQuantity);
        }

        if (parts.isEmpty()) {
            return "";
        }

        return String.join(", ", parts);
    }

    public boolean needsInstallationSpecification() {
        if (!hasRadiatorIssues()) {
            return false;
        }

        return radiatorIssues.contains(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL);
    }

    public boolean isRadiatorInstallation() {
        return installationItemType == InstallationItemType.RADIATOR;
    }

    public boolean isTowelRailInstallation() {
        return installationItemType == InstallationItemType.TOWEL_RAIL;
    }

    public boolean hasInstallationSpecification() {
        if (!needsInstallationSpecification()) {
            return true;
        }

        if (installationItemType == null) {
            return false;
        }

        if (installationPositionType == null) {
            return false;
        }

        if (installationPositionType == InstallationPositionType.DIFFERENT_POSITION && installationMoveDistance == null) {
            return false;
        }

        if (installationPositionType == InstallationPositionType.NO_EXISTING_ITEM && installationPipeDistance == null) {
            return false;
        }

        if (installationItemType == InstallationItemType.RADIATOR) {
            return radiatorConvectorType != null
                    && radiatorLengthMm != null
                    && radiatorWidthMm != null
                    && installationQuantity != null;
        }

        return towelRailLengthMm != null
                && towelRailWidthMm != null
                && installationQuantity != null;
    }

    public boolean hasInstallationItems() {
        return installationItems != null && !installationItems.isEmpty();
    }

    public CentralHeatingInstallationItem getCurrentInstallationItem() {
        if (!needsInstallationSpecification() || !hasInstallationSpecification()) {
            return null;
        }

        if (installationItemType == InstallationItemType.RADIATOR) {
            return new CentralHeatingInstallationItem(
                    installationItemType,
                    installationPositionType,
                    installationMoveDistance,
                    installationPipeDistance,
                    radiatorConvectorType,
                    radiatorLengthMm,
                    radiatorWidthMm,
                    installationQuantity
            );
        }

        return new CentralHeatingInstallationItem(
                installationItemType,
                installationPositionType,
                installationMoveDistance,
                installationPipeDistance,
                null,
                towelRailLengthMm,
                towelRailWidthMm,
                installationQuantity
        );
    }

    public List<CentralHeatingInstallationItem> getInstallationItemsPreview() {
        List<CentralHeatingInstallationItem> preview = new ArrayList<>(installationItems);
        CentralHeatingInstallationItem currentItem = getCurrentInstallationItem();

        if (currentItem != null) {
            preview.add(currentItem);
        }

        return preview;
    }

    public String getInstallationItemsSummary() {
        if (!hasInstallationItems()) {
            return "";
        }

        return installationItems.stream()
                .map(CentralHeatingInstallationItem::getSummary)
                .collect(Collectors.joining(" | "));
    }

    public void clearInstallationDraft() {
        installationItemType = null;
        installationPositionType = null;
        installationMoveDistance = null;
        installationPipeDistance = null;
        radiatorConvectorType = null;
        radiatorLengthMm = null;
        radiatorWidthMm = null;
        towelRailLengthMm = null;
        towelRailWidthMm = null;
        installationQuantity = null;
    }

    public String getRadiatorSpecificationSummary() {
        CentralHeatingInstallationItem currentItem = getCurrentInstallationItem();

        if (currentItem == null) {
            return "";
        }

        return currentItem.getSummary();
    }
}
