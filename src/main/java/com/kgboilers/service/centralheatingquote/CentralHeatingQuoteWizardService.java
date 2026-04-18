package com.kgboilers.service.centralheatingquote;

import com.kgboilers.exception.boilerinstallationquote.UnsupportedBedroomsException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedBoilerTypeException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedFuelException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedOwnershipException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedMagneticFilterException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedPowerFlushException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedPropertyTypeException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedRadiatorIssueException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedRadiatorSpecificationException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedTrvValveException;
import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import com.kgboilers.model.centralheatingquote.enums.CentralHeatingQuoteStep;
import com.kgboilers.model.centralheatingquote.enums.InstallationItemType;
import com.kgboilers.model.centralheatingquote.enums.InstallationPositionType;
import com.kgboilers.model.centralheatingquote.enums.MagneticFilterStatus;
import com.kgboilers.model.centralheatingquote.enums.PowerFlushStatus;
import com.kgboilers.model.centralheatingquote.enums.RadiatorConvectorType;
import com.kgboilers.model.centralheatingquote.enums.RadiatorIssueType;
import com.kgboilers.model.centralheatingquote.enums.TrvValveStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class CentralHeatingQuoteWizardService {
    private static final int MAX_DIMENSION_MM = 2000;
    private static final int MAX_INSTALLATION_QUANTITY = 99;
    private static final int MAX_TRV_INSTALLATION_QUANTITY = 99;

    public CentralHeatingQuoteStep startWizard(CentralHeatingQuoteSessionState state, String postcode) {
        state.setPostcode(postcode);
        state.setCurrentStep(CentralHeatingQuoteStep.PROPERTY_OWNERSHIP);
        return CentralHeatingQuoteStep.PROPERTY_OWNERSHIP;
    }

    public boolean canAccessStep(CentralHeatingQuoteSessionState state, CentralHeatingQuoteStep step) {
        if (state == null) {
            return step == CentralHeatingQuoteStep.START;
        }

        return switch (step) {
            case START -> true;
            case PROPERTY_OWNERSHIP -> state.hasPostcode();
            case PROPERTY_TYPE -> state.hasOwnership();
            case BEDROOMS -> state.hasPropertyType();
            case BOILER_TYPE -> state.hasBedrooms();
            case FUEL_TYPE -> state.hasBoilerType();
            case RADIATOR_COUNT -> state.hasFuel();
            case TRV_VALVES -> state.hasRadiatorCount();
            case POWER_FLUSH -> state.hasTrvValveStatus();
            case MAGNETIC_FILTER -> state.hasPowerFlushStatus();
            case RADIATOR_ISSUES -> state.hasMagneticFilterStatus();
            case TRV_INSTALLATION_QUANTITY -> state.hasRadiatorIssues() && state.needsTrvInstallationQuantity();
            case INSTALLATION_ITEM -> state.hasRadiatorIssues()
                    && state.needsInstallationSpecification()
                    && (!state.needsTrvInstallationQuantity() || state.hasTrvInstallationQuantity());
            case INSTALLATION_POSITION -> state.hasRadiatorIssues()
                    && state.needsInstallationSpecification()
                    && state.getInstallationItemType() != null;
            case INSTALLATION_MOVE_DISTANCE -> state.hasRadiatorIssues()
                    && state.needsInstallationSpecification()
                    && state.getInstallationItemType() != null
                    && state.getInstallationPositionType() == InstallationPositionType.DIFFERENT_POSITION;
            case INSTALLATION_PIPE_DISTANCE -> state.hasRadiatorIssues()
                    && state.needsInstallationSpecification()
                    && state.getInstallationItemType() != null
                    && state.getInstallationPositionType() == InstallationPositionType.NO_EXISTING_ITEM;
            case RADIATOR_SPECIFICATION -> state.hasRadiatorIssues()
                    && state.needsInstallationSpecification()
                    && state.getInstallationItemType() != null
                    && state.getInstallationPositionType() != null
                    && (state.getInstallationPositionType() != InstallationPositionType.DIFFERENT_POSITION
                    || state.getInstallationMoveDistance() != null)
                    && (state.getInstallationPositionType() != InstallationPositionType.NO_EXISTING_ITEM
                    || state.getInstallationPipeDistance() != null);
            case ADD_ANOTHER_INSTALLATION -> state.hasRadiatorIssues()
                    && state.needsInstallationSpecification()
                    && (state.hasInstallationSpecification() || state.hasInstallationItems());
            case SUMMARY -> state.hasRadiatorIssues()
                    && (!state.needsTrvInstallationQuantity() || state.hasTrvInstallationQuantity())
                    && (!state.needsInstallationSpecification() || state.hasInstallationItems());
        };
    }

    public CentralHeatingQuoteStep updateFuel(CentralHeatingQuoteSessionState state, FuelType selectedFuel) {
        if (selectedFuel == null) {
            throw new UnsupportedFuelException("Fuel is required");
        }

        state.setFuel(selectedFuel);
        state.setRadiatorCount(null);
        state.setCurrentStep(CentralHeatingQuoteStep.RADIATOR_COUNT);
        return CentralHeatingQuoteStep.RADIATOR_COUNT;
    }

    public CentralHeatingQuoteStep updateOwnership(CentralHeatingQuoteSessionState state, OwnershipType selectedOwnership) {
        if (selectedOwnership == null) {
            throw new UnsupportedOwnershipException("Ownership is required");
        }

        state.setOwnership(selectedOwnership);
        state.setCurrentStep(CentralHeatingQuoteStep.PROPERTY_TYPE);
        return CentralHeatingQuoteStep.PROPERTY_TYPE;
    }

    public CentralHeatingQuoteStep updatePropertyType(CentralHeatingQuoteSessionState state, PropertyType selectedPropertyType) {
        if (selectedPropertyType == null) {
            throw new UnsupportedPropertyTypeException("Property type is required");
        }

        state.setPropertyType(selectedPropertyType);
        state.setCurrentStep(CentralHeatingQuoteStep.BEDROOMS);
        return CentralHeatingQuoteStep.BEDROOMS;
    }

    public CentralHeatingQuoteStep updateBedrooms(CentralHeatingQuoteSessionState state, Bedrooms bedrooms) {
        if (bedrooms == null) {
            throw new UnsupportedBedroomsException("Unsupported bedrooms value: null");
        }

        state.setBedrooms(bedrooms);
        state.setCurrentStep(CentralHeatingQuoteStep.BOILER_TYPE);
        return CentralHeatingQuoteStep.BOILER_TYPE;
    }

    public CentralHeatingQuoteStep updateBoilerType(CentralHeatingQuoteSessionState state, BoilerType selectedBoilerType) {
        if (selectedBoilerType == null) {
            throw new UnsupportedBoilerTypeException("Boiler type is required");
        }

        if (selectedBoilerType == BoilerType.OTHER) {
            throw new UnsupportedBoilerTypeException("Unsupported boiler type: " + selectedBoilerType.getValue());
        }

        state.setBoilerType(selectedBoilerType);
        state.setCurrentStep(CentralHeatingQuoteStep.FUEL_TYPE);
        return CentralHeatingQuoteStep.FUEL_TYPE;
    }

    public CentralHeatingQuoteStep updateRadiatorCount(CentralHeatingQuoteSessionState state, RadiatorCount radiatorCount) {
        if (radiatorCount == null) {
            throw new IllegalArgumentException("Radiator count is required");
        }

        state.setRadiatorCount(radiatorCount);
        state.setTrvValveStatus(null);
        state.setCurrentStep(CentralHeatingQuoteStep.TRV_VALVES);
        return CentralHeatingQuoteStep.TRV_VALVES;
    }

    public CentralHeatingQuoteStep updateTrvValveStatus(CentralHeatingQuoteSessionState state, TrvValveStatus trvValveStatus) {
        if (trvValveStatus == null) {
            throw new UnsupportedTrvValveException("TRV valve answer is required");
        }

        state.setTrvValveStatus(trvValveStatus);
        state.setCurrentStep(CentralHeatingQuoteStep.POWER_FLUSH);
        return CentralHeatingQuoteStep.POWER_FLUSH;
    }

    public CentralHeatingQuoteStep updatePowerFlush(CentralHeatingQuoteSessionState state, PowerFlushStatus powerFlushStatus) {
        if (powerFlushStatus == null) {
            throw new UnsupportedPowerFlushException("Power flush answer is required");
        }

        state.setPowerFlushStatus(powerFlushStatus);
        state.setCurrentStep(CentralHeatingQuoteStep.MAGNETIC_FILTER);
        return CentralHeatingQuoteStep.MAGNETIC_FILTER;
    }

    public CentralHeatingQuoteStep updateMagneticFilter(CentralHeatingQuoteSessionState state,
                                                        MagneticFilterStatus magneticFilterStatus) {
        if (magneticFilterStatus == null) {
            throw new UnsupportedMagneticFilterException("Magnetic filter answer is required");
        }

        state.setMagneticFilterStatus(magneticFilterStatus);
        state.setRadiatorIssues(new LinkedHashSet<>());
        state.setOtherRadiatorIssueDetails(null);
        state.getInstallationItems().clear();
        state.clearInstallationDraft();
        state.setCurrentStep(CentralHeatingQuoteStep.RADIATOR_ISSUES);
        return CentralHeatingQuoteStep.RADIATOR_ISSUES;
    }

    public CentralHeatingQuoteStep updateRadiatorIssues(CentralHeatingQuoteSessionState state,
                                                        Set<RadiatorIssueType> radiatorIssues,
                                                        String otherIssueDetails) {
        if (radiatorIssues == null || radiatorIssues.isEmpty()) {
            throw new UnsupportedRadiatorIssueException("Please select at least one radiator issue");
        }

        if (radiatorIssues.contains(RadiatorIssueType.SOMETHING_ELSE)
                && (otherIssueDetails == null || otherIssueDetails.isBlank())) {
            throw new UnsupportedRadiatorIssueException("Please describe the issue in Something else");
        }

        state.setRadiatorIssues(new LinkedHashSet<>(radiatorIssues));
        state.setOtherRadiatorIssueDetails(
                radiatorIssues.contains(RadiatorIssueType.SOMETHING_ELSE) ? otherIssueDetails.trim() : null
        );
        state.setTrvValvesQuantity(null);
        state.setLockshieldValvesQuantity(null);
        state.setTowelRailValvesQuantity(null);
        if (state.needsInstallationSpecification()) {
            state.getInstallationItems().clear();
            state.clearInstallationDraft();
        } else {
            state.getInstallationItems().clear();
            state.clearInstallationDraft();
        }

        if (state.needsTrvInstallationQuantity()) {
            state.setCurrentStep(CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY);
            return CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY;
        }

        if (state.needsInstallationSpecification()) {
            state.setCurrentStep(CentralHeatingQuoteStep.INSTALLATION_ITEM);
            return CentralHeatingQuoteStep.INSTALLATION_ITEM;
        }

        state.setCurrentStep(CentralHeatingQuoteStep.SUMMARY);
        return CentralHeatingQuoteStep.SUMMARY;
    }

    public CentralHeatingQuoteStep updateTrvInstallationQuantity(CentralHeatingQuoteSessionState state,
                                                                 Integer trvValvesQuantity,
                                                                 Integer lockshieldValvesQuantity,
                                                                 Integer towelRailValvesQuantity) {
        if (!state.needsTrvInstallationQuantity()) {
            throw new UnsupportedRadiatorIssueException("TRV installation quantity is not required for this selection");
        }

        int normalizedTrvQuantity = normalizeValveQuantity(trvValvesQuantity);
        int normalizedLockshieldQuantity = normalizeValveQuantity(lockshieldValvesQuantity);
        int normalizedTowelRailQuantity = normalizeValveQuantity(towelRailValvesQuantity);

        if (normalizedTrvQuantity == 0 && normalizedLockshieldQuantity == 0 && normalizedTowelRailQuantity == 0) {
            throw new UnsupportedRadiatorIssueException("Please enter how many valves you need installed");
        }

        state.setTrvValvesQuantity(normalizedTrvQuantity == 0 ? null : normalizedTrvQuantity);
        state.setLockshieldValvesQuantity(normalizedLockshieldQuantity == 0 ? null : normalizedLockshieldQuantity);
        state.setTowelRailValvesQuantity(normalizedTowelRailQuantity == 0 ? null : normalizedTowelRailQuantity);

        if (state.needsInstallationSpecification()) {
            state.setCurrentStep(CentralHeatingQuoteStep.INSTALLATION_ITEM);
            return CentralHeatingQuoteStep.INSTALLATION_ITEM;
        }

        state.setCurrentStep(CentralHeatingQuoteStep.SUMMARY);
        return CentralHeatingQuoteStep.SUMMARY;
    }

    private int normalizeValveQuantity(Integer quantity) {
        if (quantity == null) {
            return 0;
        }

        if (quantity < 0) {
            throw new UnsupportedRadiatorIssueException("Please enter a valid valve quantity");
        }

        if (quantity > MAX_TRV_INSTALLATION_QUANTITY) {
            throw new UnsupportedRadiatorIssueException("Maximum quantity is 99");
        }

        return quantity;
    }

    public CentralHeatingQuoteStep updateInstallationItem(CentralHeatingQuoteSessionState state,
                                                          InstallationItemType installationItemType) {
        if (!state.needsInstallationSpecification()) {
            throw new UnsupportedRadiatorSpecificationException("Installation item is not required for this selection");
        }

        if (installationItemType == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose radiator or towel rail");
        }

        state.setInstallationItemType(installationItemType);
        state.setInstallationPositionType(null);
        state.setInstallationMoveDistance(null);
        state.setInstallationPipeDistance(null);
        state.setRadiatorConvectorType(null);
        state.setRadiatorLengthMm(null);
        state.setRadiatorWidthMm(null);
        state.setTowelRailLengthMm(null);
        state.setTowelRailWidthMm(null);
        state.setInstallationQuantity(null);
        state.setCurrentStep(CentralHeatingQuoteStep.INSTALLATION_POSITION);
        return CentralHeatingQuoteStep.INSTALLATION_POSITION;
    }

    public CentralHeatingQuoteStep updateInstallationPosition(CentralHeatingQuoteSessionState state,
                                                              InstallationPositionType installationPositionType) {
        if (!state.needsInstallationSpecification()) {
            throw new UnsupportedRadiatorSpecificationException("Installation position is not required for this selection");
        }

        if (state.getInstallationItemType() == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose radiator or towel rail");
        }

        if (installationPositionType == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose same position, different position or no existing item there");
        }

        state.setInstallationPositionType(installationPositionType);
        state.setInstallationMoveDistance(null);
        state.setInstallationPipeDistance(null);
        state.setRadiatorConvectorType(null);
        state.setRadiatorLengthMm(null);
        state.setRadiatorWidthMm(null);
        state.setTowelRailLengthMm(null);
        state.setTowelRailWidthMm(null);
        state.setInstallationQuantity(null);
        if (installationPositionType == InstallationPositionType.DIFFERENT_POSITION) {
            state.setCurrentStep(CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE);
            return CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE;
        }

        if (installationPositionType == InstallationPositionType.NO_EXISTING_ITEM) {
            state.setCurrentStep(CentralHeatingQuoteStep.INSTALLATION_PIPE_DISTANCE);
            return CentralHeatingQuoteStep.INSTALLATION_PIPE_DISTANCE;
        }

        state.setCurrentStep(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION);
        return CentralHeatingQuoteStep.RADIATOR_SPECIFICATION;
    }

    public CentralHeatingQuoteStep updateInstallationMoveDistance(CentralHeatingQuoteSessionState state,
                                                                  RelocationDistance moveDistance) {
        if (!state.needsInstallationSpecification()) {
            throw new UnsupportedRadiatorSpecificationException("Installation move distance is not required for this selection");
        }

        if (state.getInstallationItemType() == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose radiator or towel rail");
        }

        if (state.getInstallationPositionType() != InstallationPositionType.DIFFERENT_POSITION) {
            throw new UnsupportedRadiatorSpecificationException("Move distance is only required for different position");
        }

        if (moveDistance == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose how far you want to move it");
        }

        state.setInstallationMoveDistance(moveDistance);
        state.setCurrentStep(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION);
        return CentralHeatingQuoteStep.RADIATOR_SPECIFICATION;
    }

    public CentralHeatingQuoteStep updateInstallationPipeDistance(CentralHeatingQuoteSessionState state,
                                                                  RelocationDistance pipeDistance) {
        if (!state.needsInstallationSpecification()) {
            throw new UnsupportedRadiatorSpecificationException("Installation pipe distance is not required for this selection");
        }

        if (state.getInstallationItemType() == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose radiator or towel rail");
        }

        if (state.getInstallationPositionType() != InstallationPositionType.NO_EXISTING_ITEM) {
            throw new UnsupportedRadiatorSpecificationException("Pipe distance is only required when there is no existing item there");
        }

        if (pipeDistance == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose how far the nearest heating pipe is");
        }

        state.setInstallationPipeDistance(pipeDistance);
        state.setCurrentStep(CentralHeatingQuoteStep.RADIATOR_SPECIFICATION);
        return CentralHeatingQuoteStep.RADIATOR_SPECIFICATION;
    }

    public CentralHeatingQuoteStep updateRadiatorSpecification(CentralHeatingQuoteSessionState state,
                                                               RadiatorConvectorType radiatorConvectorType,
                                                               Integer radiatorLengthMm,
                                                               Integer radiatorWidthMm,
                                                               Integer towelRailLengthMm,
                                                               Integer towelRailWidthMm,
                                                               Integer installationQuantity) {
        if (!state.needsInstallationSpecification()) {
            throw new UnsupportedRadiatorSpecificationException("Installation specification is not required for this selection");
        }

        InstallationItemType installationItemType = state.getInstallationItemType();

        if (installationItemType == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose radiator or towel rail");
        }

        if (state.getInstallationPositionType() == null) {
            throw new UnsupportedRadiatorSpecificationException("Please choose same position, different position or no existing item there");
        }

        if (installationItemType == InstallationItemType.RADIATOR) {
            if (radiatorConvectorType == null) {
                throw new UnsupportedRadiatorSpecificationException("Please select single convector or double convector");
            }

            validateDimension(radiatorLengthMm, "radiator length");
            validateDimension(radiatorWidthMm, "radiator width");
        }

        if (installationItemType == InstallationItemType.TOWEL_RAIL) {
            validateDimension(towelRailLengthMm, "towel rail length");
            validateDimension(towelRailWidthMm, "towel rail width");
        }

        if (installationQuantity == null || installationQuantity <= 0) {
            throw new UnsupportedRadiatorSpecificationException("Please enter the quantity");
        }

        if (installationQuantity > MAX_INSTALLATION_QUANTITY) {
            throw new UnsupportedRadiatorSpecificationException("Maximum quantity is 99");
        }

        state.setInstallationItemType(installationItemType);
        state.setRadiatorConvectorType(installationItemType == InstallationItemType.RADIATOR ? radiatorConvectorType : null);
        state.setRadiatorLengthMm(installationItemType == InstallationItemType.RADIATOR ? radiatorLengthMm : null);
        state.setRadiatorWidthMm(installationItemType == InstallationItemType.RADIATOR ? radiatorWidthMm : null);
        state.setTowelRailLengthMm(installationItemType == InstallationItemType.TOWEL_RAIL ? towelRailLengthMm : null);
        state.setTowelRailWidthMm(installationItemType == InstallationItemType.TOWEL_RAIL ? towelRailWidthMm : null);
        state.setInstallationQuantity(installationQuantity);
        state.setCurrentStep(CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION);
        return CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION;
    }

    private void validateDimension(Integer value, String fieldLabel) {
        if (value == null || value <= 0) {
            throw new UnsupportedRadiatorSpecificationException("Please enter the " + fieldLabel + " in mm");
        }

        if (value > MAX_DIMENSION_MM) {
            throw new UnsupportedRadiatorSpecificationException(
                    fieldLabel.contains("width")
                            ? "Maximum width is " + MAX_DIMENSION_MM + " mm"
                            : "Maximum length is " + MAX_DIMENSION_MM + " mm"
            );
        }
    }

    public CentralHeatingQuoteStep updateAddAnotherInstallation(CentralHeatingQuoteSessionState state, boolean addAnother) {
        if (!state.needsInstallationSpecification()) {
            throw new UnsupportedRadiatorSpecificationException("Add another installation is not required for this selection");
        }

        if (state.getCurrentInstallationItem() != null) {
            state.getInstallationItems().add(state.getCurrentInstallationItem());
            state.clearInstallationDraft();
        }

        if (!state.hasInstallationItems()) {
            throw new UnsupportedRadiatorSpecificationException("Please add at least one radiator or towel rail");
        }

        if (addAnother) {
            state.setCurrentStep(CentralHeatingQuoteStep.INSTALLATION_ITEM);
            return CentralHeatingQuoteStep.INSTALLATION_ITEM;
        }

        state.setCurrentStep(CentralHeatingQuoteStep.SUMMARY);
        return CentralHeatingQuoteStep.SUMMARY;
    }
}
