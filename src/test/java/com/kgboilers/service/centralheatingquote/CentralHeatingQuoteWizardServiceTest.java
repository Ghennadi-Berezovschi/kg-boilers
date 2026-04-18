package com.kgboilers.service.centralheatingquote;

import com.kgboilers.exception.boilerinstallationquote.UnsupportedRadiatorSpecificationException;
import com.kgboilers.exception.boilerinstallationquote.UnsupportedRadiatorIssueException;
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
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CentralHeatingQuoteWizardServiceTest {

    private final CentralHeatingQuoteWizardService service = new CentralHeatingQuoteWizardService();

    @Test
    void startWizard_shouldGoToFuelType() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();

        CentralHeatingQuoteStep nextStep = service.startWizard(state, "E16 4JJ");

        assertEquals(CentralHeatingQuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertEquals("E16 4JJ", state.getPostcode());
        assertEquals(CentralHeatingQuoteStep.PROPERTY_OWNERSHIP, state.getCurrentStep());
    }

    @Test
    void updateSharedSteps_shouldEndAtComingSoon() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();

        assertEquals(CentralHeatingQuoteStep.PROPERTY_TYPE, service.updateOwnership(state, OwnershipType.HOMEOWNER));
        assertEquals(CentralHeatingQuoteStep.BEDROOMS, service.updatePropertyType(state, PropertyType.HOUSE));
        assertEquals(CentralHeatingQuoteStep.BOILER_TYPE, service.updateBedrooms(state, Bedrooms.THREE));
        assertEquals(CentralHeatingQuoteStep.FUEL_TYPE, service.updateBoilerType(state, BoilerType.COMBI));
        assertEquals(CentralHeatingQuoteStep.RADIATOR_COUNT, service.updateFuel(state, FuelType.GAS));
        assertEquals(CentralHeatingQuoteStep.TRV_VALVES, service.updateRadiatorCount(state, RadiatorCount.SIX_TO_NINE));
        assertEquals(CentralHeatingQuoteStep.POWER_FLUSH, service.updateTrvValveStatus(state, TrvValveStatus.NOT_ALL_OF_THEM));
        assertEquals(CentralHeatingQuoteStep.MAGNETIC_FILTER, service.updatePowerFlush(state, PowerFlushStatus.YES_DONE));
        assertEquals(CentralHeatingQuoteStep.RADIATOR_ISSUES, service.updateMagneticFilter(state, MagneticFilterStatus.YES_HAS));
        assertEquals(CentralHeatingQuoteStep.SUMMARY, service.updateRadiatorIssues(
                state,
                Set.of(RadiatorIssueType.RADIATOR_LEAK, RadiatorIssueType.SOMETHING_ELSE),
                "Radiator in hallway is leaking"
        ));
    }

    @Test
    void updateFuel_shouldAllowNonGasFuelTypes() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setBoilerType(BoilerType.COMBI);

        assertEquals(CentralHeatingQuoteStep.RADIATOR_COUNT, service.updateFuel(state, FuelType.LPG));
        assertEquals(FuelType.LPG, state.getFuel());

        assertEquals(CentralHeatingQuoteStep.RADIATOR_COUNT, service.updateFuel(state, FuelType.OIL));
        assertEquals(FuelType.OIL, state.getFuel());

        assertEquals(CentralHeatingQuoteStep.RADIATOR_COUNT, service.updateFuel(state, FuelType.ELECTRIC));
        assertEquals(FuelType.ELECTRIC, state.getFuel());
    }

    @Test
    void updateRadiatorCount_shouldRedirectToPowerFlush() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setFuel(FuelType.GAS);

        assertEquals(CentralHeatingQuoteStep.TRV_VALVES, service.updateRadiatorCount(state, RadiatorCount.TEN_TO_THIRTEEN));
        assertEquals(RadiatorCount.TEN_TO_THIRTEEN, state.getRadiatorCount());
        assertEquals("10-13 radiators", state.getRadiatorCountSummary());
    }

    @Test
    void updateTrvValveStatus_shouldRedirectToPowerFlush() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorCount(RadiatorCount.SIX_TO_NINE);

        assertEquals(CentralHeatingQuoteStep.POWER_FLUSH, service.updateTrvValveStatus(state, TrvValveStatus.NOT_ALL_OF_THEM));
        assertEquals(TrvValveStatus.NOT_ALL_OF_THEM, state.getTrvValveStatus());
        assertEquals("Not all of them", state.getTrvValveStatusSummary());
    }

    @Test
    void updateRadiatorIssues_shouldSaveIssuesAndOptionalOtherText() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setMagneticFilterStatus(MagneticFilterStatus.YES_HAS);

        assertEquals(CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY, service.updateRadiatorIssues(
                state,
                Set.of(RadiatorIssueType.INSTALL_TRV_VALVES, RadiatorIssueType.SOMETHING_ELSE),
                "Bathroom radiator needs checking"
        ));
        String summary = state.getRadiatorIssuesSummary();
        assertTrue(summary.contains("Install TRV valves, Lockshield valves, Towel rail valves"));
        assertTrue(summary.contains("Something else"));
        assertEquals("Bathroom radiator needs checking", state.getOtherRadiatorIssueDetails());
    }

    @Test
    void updateRadiatorIssues_shouldRedirectToTrvInstallationQuantity_whenTrvInstallationSelected() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setMagneticFilterStatus(MagneticFilterStatus.YES_HAS);

        assertEquals(CentralHeatingQuoteStep.TRV_INSTALLATION_QUANTITY, service.updateRadiatorIssues(
                state,
                Set.of(RadiatorIssueType.INSTALL_TRV_VALVES),
                null
        ));
    }

    @Test
    void updateRadiatorIssues_shouldRedirectToInstallationItem_whenInstallSelected() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setMagneticFilterStatus(MagneticFilterStatus.YES_HAS);

        assertEquals(CentralHeatingQuoteStep.INSTALLATION_ITEM, service.updateRadiatorIssues(
                state,
                Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL),
                null
        ));
    }

    @Test
    void updateTrvInstallationQuantity_shouldRedirectToComingSoon_whenNoInstallationItemIsNeeded() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_TRV_VALVES));

        assertEquals(CentralHeatingQuoteStep.SUMMARY, service.updateTrvInstallationQuantity(state, 8, 8, 2));
        assertEquals(8, state.getTrvValvesQuantity());
        assertEquals(8, state.getLockshieldValvesQuantity());
        assertEquals(2, state.getTowelRailValvesQuantity());
    }

    @Test
    void updateTrvInstallationQuantity_shouldRedirectToInstallationItem_whenInstallationIsAlsoNeeded() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(
                RadiatorIssueType.INSTALL_TRV_VALVES,
                RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL
        ));

        assertEquals(CentralHeatingQuoteStep.INSTALLATION_ITEM, service.updateTrvInstallationQuantity(state, 4, 4, 0));
        assertEquals(4, state.getTrvValvesQuantity());
        assertEquals(4, state.getLockshieldValvesQuantity());
        assertNull(state.getTowelRailValvesQuantity());
    }

    @Test
    void updateTrvInstallationQuantity_shouldRejectQuantityAbove99() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_TRV_VALVES));

        UnsupportedRadiatorIssueException exception = assertThrows(
                UnsupportedRadiatorIssueException.class,
                () -> service.updateTrvInstallationQuantity(state, 100, 0, 0)
        );

        assertEquals("Maximum quantity is 99", exception.getMessage());
    }

    @Test
    void updateTrvInstallationQuantity_shouldRequireAtLeastOneValveType() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_TRV_VALVES));

        UnsupportedRadiatorIssueException exception = assertThrows(
                UnsupportedRadiatorIssueException.class,
                () -> service.updateTrvInstallationQuantity(state, 0, 0, 0)
        );

        assertEquals("Please enter how many valves you need installed", exception.getMessage());
    }

    @Test
    void updateInstallationItem_shouldRedirectToInstallationPosition() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));

        assertTrue(service.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_ITEM));

        assertEquals(
                CentralHeatingQuoteStep.INSTALLATION_POSITION,
                service.updateInstallationItem(state, InstallationItemType.TOWEL_RAIL)
        );
    }

    @Test
    void updateInstallationPosition_shouldRedirectToRadiatorSpecification() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));

        service.updateInstallationItem(state, InstallationItemType.TOWEL_RAIL);

        assertEquals(
                CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE,
                service.updateInstallationPosition(state, InstallationPositionType.DIFFERENT_POSITION)
        );
    }

    @Test
    void updateInstallationPosition_shouldAllowNoExistingItemThere() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));

        service.updateInstallationItem(state, InstallationItemType.RADIATOR);

        assertEquals(
                CentralHeatingQuoteStep.INSTALLATION_PIPE_DISTANCE,
                service.updateInstallationPosition(state, InstallationPositionType.NO_EXISTING_ITEM)
        );
    }

    @Test
    void updateInstallationMoveDistance_shouldRedirectToRadiatorSpecification() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));

        service.updateInstallationItem(state, InstallationItemType.RADIATOR);
        service.updateInstallationPosition(state, InstallationPositionType.DIFFERENT_POSITION);

        assertEquals(
                CentralHeatingQuoteStep.RADIATOR_SPECIFICATION,
                service.updateInstallationMoveDistance(state, RelocationDistance.TWO_TO_THREE)
        );
    }

    @Test
    void updateInstallationPipeDistance_shouldRedirectToRadiatorSpecification() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));

        service.updateInstallationItem(state, InstallationItemType.TOWEL_RAIL);
        service.updateInstallationPosition(state, InstallationPositionType.NO_EXISTING_ITEM);

        assertEquals(
                CentralHeatingQuoteStep.RADIATOR_SPECIFICATION,
                service.updateInstallationPipeDistance(state, RelocationDistance.FOUR_TO_FIVE)
        );
    }

    @Test
    void canAccessStep_shouldAllowRadiatorSpecificationAndComingSoon_afterTowelRailSelection() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));

        service.updateInstallationItem(state, InstallationItemType.TOWEL_RAIL);
        service.updateInstallationPosition(state, InstallationPositionType.DIFFERENT_POSITION);

        assertTrue(service.canAccessStep(state, CentralHeatingQuoteStep.INSTALLATION_MOVE_DISTANCE));

        service.updateInstallationMoveDistance(state, RelocationDistance.TWO_TO_THREE);

        assertTrue(service.canAccessStep(state, CentralHeatingQuoteStep.RADIATOR_SPECIFICATION));

        service.updateRadiatorSpecification(state, null, null, null, 800, 500, 1);

        assertTrue(service.canAccessStep(state, CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION));

        service.updateAddAnotherInstallation(state, false);

        assertTrue(service.canAccessStep(state, CentralHeatingQuoteStep.SUMMARY));
    }

    @Test
    void updateRadiatorSpecification_shouldSaveDimensionsAndType() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.RADIATOR);
        state.setInstallationPositionType(InstallationPositionType.SAME_POSITION);

        assertEquals(CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION, service.updateRadiatorSpecification(
                state,
                RadiatorConvectorType.DOUBLE_CONVECTOR,
                1200,
                600,
                null,
                null,
                2
        ));
        assertEquals("Radiator: Same position, Double convector, 1200mm x 600mm, qty 2", state.getRadiatorSpecificationSummary());
    }

    @Test
    void updateRadiatorSpecification_shouldSaveTowelRailDimensions_whenTowelRailSelected() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.TOWEL_RAIL);
        state.setInstallationPositionType(InstallationPositionType.DIFFERENT_POSITION);
        state.setInstallationMoveDistance(RelocationDistance.TWO_TO_THREE);

        assertEquals(CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION, service.updateRadiatorSpecification(
                state,
                null,
                null,
                null,
                800,
                500,
                1
        ));
        assertEquals("Towel rail: Different position (2-3 metres), 800mm x 500mm, qty 1", state.getRadiatorSpecificationSummary());
    }

    @Test
    void updateRadiatorSpecification_shouldSaveNearestPipeDistance_whenNoExistingItemThere() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.RADIATOR);
        state.setInstallationPositionType(InstallationPositionType.NO_EXISTING_ITEM);
        state.setInstallationPipeDistance(RelocationDistance.FOUR_TO_FIVE);

        assertEquals(CentralHeatingQuoteStep.ADD_ANOTHER_INSTALLATION, service.updateRadiatorSpecification(
                state,
                RadiatorConvectorType.SINGLE_CONVECTOR,
                1000,
                500,
                null,
                null,
                1
        ));
        assertEquals("Radiator: No existing item there (nearest pipe 4-5 metres), Single convector, 1000mm x 500mm, qty 1", state.getRadiatorSpecificationSummary());
    }

    @Test
    void updateRadiatorSpecification_shouldRejectDimensionsAbove2000mm() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.RADIATOR);
        state.setInstallationPositionType(InstallationPositionType.SAME_POSITION);

        UnsupportedRadiatorSpecificationException exception = assertThrows(
                UnsupportedRadiatorSpecificationException.class,
                () -> service.updateRadiatorSpecification(
                        state,
                        RadiatorConvectorType.SINGLE_CONVECTOR,
                        2001,
                        600,
                        null,
                        null,
                        1
                )
        );

        assertEquals("Maximum length is 2000 mm", exception.getMessage());
    }

    @Test
    void updateRadiatorSpecification_shouldRejectQuantityAbove99() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.TOWEL_RAIL);
        state.setInstallationPositionType(InstallationPositionType.SAME_POSITION);

        UnsupportedRadiatorSpecificationException exception = assertThrows(
                UnsupportedRadiatorSpecificationException.class,
                () -> service.updateRadiatorSpecification(
                        state,
                        null,
                        null,
                        null,
                        800,
                        500,
                        100
                )
        );

        assertEquals("Maximum quantity is 99", exception.getMessage());
    }

    @Test
    void updateAddAnotherInstallation_shouldSaveCurrentDraftAndReturnInstallationItem_whenYes() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.RADIATOR);
        state.setInstallationPositionType(InstallationPositionType.SAME_POSITION);
        state.setRadiatorConvectorType(RadiatorConvectorType.SINGLE_CONVECTOR);
        state.setRadiatorLengthMm(600);
        state.setRadiatorWidthMm(1200);
        state.setInstallationQuantity(2);

        assertEquals(CentralHeatingQuoteStep.INSTALLATION_ITEM, service.updateAddAnotherInstallation(state, true));
        assertEquals(1, state.getInstallationItems().size());
        assertNull(state.getInstallationItemType());
        assertNull(state.getInstallationPositionType());
    }

    @Test
    void updateAddAnotherInstallation_shouldSaveCurrentDraftAndReturnComingSoon_whenNo() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setRadiatorIssues(Set.of(RadiatorIssueType.INSTALL_RADIATOR_OR_TOWEL_RAIL));
        state.setInstallationItemType(InstallationItemType.TOWEL_RAIL);
        state.setInstallationPositionType(InstallationPositionType.DIFFERENT_POSITION);
        state.setInstallationMoveDistance(RelocationDistance.TWO_TO_THREE);
        state.setTowelRailLengthMm(800);
        state.setTowelRailWidthMm(500);
        state.setInstallationQuantity(1);

        assertEquals(CentralHeatingQuoteStep.SUMMARY, service.updateAddAnotherInstallation(state, false));
        assertEquals(1, state.getInstallationItems().size());
        assertEquals("Towel rail: Different position (2-3 metres), 800mm x 500mm, qty 1", state.getInstallationItems().getFirst().getSummary());
    }

    @Test
    void updateRadiatorIssues_shouldClearOtherTextWhenSomethingElseIsNotSelected() {
        CentralHeatingQuoteSessionState state = new CentralHeatingQuoteSessionState();
        state.setMagneticFilterStatus(MagneticFilterStatus.NO_DOES_NOT_HAVE);

        assertEquals(CentralHeatingQuoteStep.SUMMARY, service.updateRadiatorIssues(
                state,
                Set.of(RadiatorIssueType.RADIATOR_VALVE_ISSUE),
                "Should be ignored"
        ));
        assertNull(state.getOtherRadiatorIssueDetails());
    }
}
