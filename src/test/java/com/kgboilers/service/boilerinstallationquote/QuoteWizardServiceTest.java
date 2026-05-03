package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.exception.boilerinstallationquote.UnsupportedBedroomsException;
import com.kgboilers.model.boilerinstallationquote.GasApplianceSelection;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.BoilerFloorLevel;
import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.BoilerPosition;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueClearance;
import com.kgboilers.model.boilerinstallation.enums.HorizontalFlueShape;
import com.kgboilers.model.boilerinstallation.enums.FluePropertyDistance;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import com.kgboilers.model.boilerinstallation.enums.FluePosition;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.GasApplianceType;
import com.kgboilers.model.boilerinstallation.enums.GasSafetyServiceType;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.boilerinstallation.enums.SlopedRoofPosition;
import com.kgboilers.model.boilerinstallation.enums.VerticalFlueType;
import com.kgboilers.model.boilerrepair.enums.BoilerAge;
import com.kgboilers.model.boilerrepair.enums.BoilerPressureStatus;
import com.kgboilers.model.boilerrepair.enums.FaultCodeDisplayStatus;
import com.kgboilers.model.boilerrepair.enums.MagneticFilterStatus;
import com.kgboilers.model.boilerrepair.enums.PowerFlushStatus;
import com.kgboilers.model.boilerrepair.enums.RepairProblem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoteWizardServiceTest {

    private final QuoteWizardService service = new QuoteWizardService();

    @Test
    void startWizard_shouldSetPostcodeAndStep() {
        QuoteSessionState state = new QuoteSessionState();
        String postcode = "E16 4JJ";

        QuoteStep nextStep = service.startWizard(state, postcode);

        assertEquals(QuoteStep.FUEL_TYPE, nextStep);
        assertEquals(postcode, state.getPostcode());
        assertEquals(QuoteStep.FUEL_TYPE, state.getCurrentStep());
    }

    @Test
    void startWizard_shouldSkipFuelForHotWaterCylinder() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.startWizard(state, "E16 4JJ", "hot-water-cylinder");

        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertNull(state.getFuel());
        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, state.getCurrentStep());
        assertEquals("E16 4JJ", state.getPostcode());
    }

    @Test
    void startWizard_shouldSkipFuelForGasPipework() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.startWizard(state, "E16 4JJ", "gas-pipework-and-gas-leak-detection");

        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertNull(state.getFuel());
        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, state.getCurrentStep());
    }

    @Test
    void canAccessStep_shouldAllowOwnershipWithoutFuelForHotWaterCylinder() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");

        assertEquals(false, service.canAccessStep(state, QuoteStep.FUEL_TYPE, "hot-water-cylinder"));
        assertEquals(true, service.canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP, "hot-water-cylinder"));
    }

    @Test
    void updateFuel_shouldSetFuelAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");

        QuoteStep nextStep = service.updateFuel(state, FuelType.GAS);

        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertEquals(FuelType.GAS, state.getFuel());
    }

    @Test
    void updateFuel_shouldAllowElectricForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");

        QuoteStep nextStep = service.updateFuel(state, FuelType.ELECTRIC, "boiler-repair");

        assertEquals(QuoteStep.BOILER_TYPE, nextStep);
        assertEquals(FuelType.ELECTRIC, state.getFuel());
    }

    @Test
    void updateFuel_shouldAllowElectricForGeneralFlow() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");

        QuoteStep nextStep = service.updateFuel(state, FuelType.ELECTRIC);

        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertEquals(FuelType.ELECTRIC, state.getFuel());
    }

    @Test
    void updateFuel_shouldAllowUnknownOtherForGeneralFlow() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");

        QuoteStep nextStep = service.updateFuel(state, FuelType.UNKNOWN_OTHER);

        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertEquals(FuelType.UNKNOWN_OTHER, state.getFuel());
    }

    @Test
    void updateOwnership_shouldSetOwnershipAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateOwnership(state, OwnershipType.HOMEOWNER);

        assertEquals(QuoteStep.PROPERTY_TYPE, nextStep);
        assertEquals(OwnershipType.HOMEOWNER, state.getOwnership());
    }

    @Test
    void updatePropertyType_shouldSetPropertyTypeAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updatePropertyType(state, PropertyType.HOUSE);

        assertEquals(QuoteStep.BEDROOMS, nextStep);
        assertEquals(PropertyType.HOUSE, state.getPropertyType());
    }

    @Test
    void updatePropertyType_shouldSkipBedroomsForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updatePropertyType(state, PropertyType.HOUSE, "boiler-repair");

        assertEquals(QuoteStep.BOILER_TYPE, nextStep);
        assertEquals(PropertyType.HOUSE, state.getPropertyType());
    }

    @Test
    void updatePropertyType_shouldSkipBedroomsForHotWaterCylinder() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updatePropertyType(state, PropertyType.HOUSE, "hot-water-cylinder");

        assertEquals(QuoteStep.BOILER_TYPE, nextStep);
        assertEquals(PropertyType.HOUSE, state.getPropertyType());
        assertNull(state.getBedrooms());
    }

    @Test
    void updatePropertyType_shouldAskGasAppliancesForGasPipework() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updatePropertyType(state, PropertyType.HOUSE, "gas-pipework-and-gas-leak-detection");

        assertEquals(QuoteStep.GAS_APPLIANCES, nextStep);
        assertEquals(PropertyType.HOUSE, state.getPropertyType());
    }

    @Test
    void updateGasAppliances_shouldReturnProblemDetailsForGasPipework() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateGasAppliances(
                state,
                List.of(new GasApplianceSelection(GasApplianceType.GAS_BOILER, 1)),
                "gas-pipework-and-gas-leak-detection"
        );

        assertEquals(QuoteStep.PROBLEM_DETAILS, nextStep);
        assertEquals("Gas boiler", state.getGasAppliancesSummary());
    }

    @Test
    void updateProblemDetails_shouldReturnContactForGasPipework() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateProblemDetails(
                state,
                "Gas pipe leaking near cooker",
                "gas-pipework-and-gas-leak-detection"
        );

        assertEquals(QuoteStep.CONTACT, nextStep);
        assertEquals("Gas pipe leaking near cooker", state.getProblemDetails());
    }

    @Test
    void updateGasSafetyServiceType_shouldAskBoilerTypeForBoilerService() {
        QuoteSessionState state = new QuoteSessionState();
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);

        QuoteStep nextStep = service.updateGasSafetyServiceType(state, GasSafetyServiceType.BOILER_SERVICE);

        assertEquals(QuoteStep.BOILER_TYPE, nextStep);
        assertEquals(GasSafetyServiceType.BOILER_SERVICE, state.getGasSafetyServiceType());
        assertNull(state.getOwnership());
        assertNull(state.getPropertyType());
    }

    @Test
    void updateGasSafetyServiceType_shouldAskBoilerTypeForBoilerServiceAndCertificate() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateGasSafetyServiceType(
                state,
                GasSafetyServiceType.BOILER_SERVICE_AND_GAS_SAFETY_CERTIFICATE
        );

        assertEquals(QuoteStep.BOILER_TYPE, nextStep);
        assertEquals(QuoteStep.BOILER_TYPE, state.getCurrentStep());
    }

    @Test
    void updateGasSafetyServiceType_shouldSkipBoilerTypeForGasCertificateOnly() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateGasSafetyServiceType(state, GasSafetyServiceType.GAS_SAFETY_CERTIFICATE);

        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, state.getCurrentStep());
    }

    @Test
    void updateBoilerType_shouldSetBoilerTypeAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerType(state, BoilerType.COMBI);

        assertEquals(QuoteStep.BOILER_POSITION, nextStep);
        assertEquals(BoilerType.COMBI, state.getBoilerType());
    }

    @Test
    void updateBoilerType_shouldReturnBoilerMakeForHotWaterCylinder() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);

        QuoteStep nextStep = service.updateBoilerType(state, BoilerType.SYSTEM, "hot-water-cylinder");

        assertEquals(QuoteStep.BOILER_MAKE, nextStep);
        assertEquals(BoilerType.SYSTEM, state.getBoilerType());
        assertEquals(QuoteStep.BOILER_MAKE, state.getCurrentStep());
        assertEquals(false, service.isComplete(state, "hot-water-cylinder"));
        assertEquals(true, service.canAccessStep(state, QuoteStep.BOILER_MAKE, "hot-water-cylinder"));
        assertEquals(false, service.canAccessStep(state, QuoteStep.BOILER_POSITION, "hot-water-cylinder"));
    }

    @Test
    void updateBoilerMake_shouldReturnHotWaterForHotWaterCylinder() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);
        state.setBoilerType(BoilerType.SYSTEM);

        QuoteStep nextStep = service.updateBoilerMake(state, BoilerMake.VAILLANT, "hot-water-cylinder");

        assertEquals(QuoteStep.HOT_WATER, nextStep);
        assertEquals(BoilerMake.VAILLANT, state.getBoilerMake());
        assertEquals(QuoteStep.HOT_WATER, state.getCurrentStep());
        assertEquals(false, service.isComplete(state, "hot-water-cylinder"));
        assertEquals(true, service.canAccessStep(state, QuoteStep.HOT_WATER, "hot-water-cylinder"));
    }

    @Test
    void updateBoilerType_shouldReturnHotWaterForHotWaterCylinderWithoutGasBoiler() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);

        QuoteStep nextStep = service.updateBoilerType(state, BoilerType.OTHER, "hot-water-cylinder");

        assertEquals(QuoteStep.HOT_WATER, nextStep);
        assertEquals(BoilerType.OTHER, state.getBoilerType());
        assertEquals(QuoteStep.HOT_WATER, state.getCurrentStep());
        assertEquals(false, service.isComplete(state, "hot-water-cylinder"));
        assertEquals(true, service.canAccessStep(state, QuoteStep.HOT_WATER, "hot-water-cylinder"));
        assertEquals(false, service.canAccessStep(state, QuoteStep.BOILER_MAKE, "hot-water-cylinder"));
    }

    @Test
    void updateHotWater_shouldReturnProblemDetailsForHotWaterCylinder() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);
        state.setBoilerType(BoilerType.SYSTEM);
        state.setBoilerMake(BoilerMake.VAILLANT);

        QuoteStep nextStep = service.updateHotWater(state, true, "hot-water-cylinder");

        assertEquals(QuoteStep.PROBLEM_DETAILS, nextStep);
        assertEquals(true, state.getHotWaterAvailable());
        assertEquals(QuoteStep.PROBLEM_DETAILS, state.getCurrentStep());
        assertEquals(false, service.isComplete(state, "hot-water-cylinder"));
        assertEquals(true, service.canAccessStep(state, QuoteStep.PROBLEM_DETAILS, "hot-water-cylinder"));
    }

    @Test
    void updateProblemDetails_shouldReturnContactForHotWaterCylinder() {
        QuoteSessionState state = new QuoteSessionState();
        state.setPostcode("E16 4JJ");
        state.setOwnership(OwnershipType.HOMEOWNER);
        state.setPropertyType(PropertyType.HOUSE);
        state.setBoilerType(BoilerType.SYSTEM);
        state.setBoilerMake(BoilerMake.VAILLANT);
        state.setHotWaterAvailable(true);

        QuoteStep nextStep = service.updateProblemDetails(state, "Cylinder is leaking", "hot-water-cylinder");

        assertEquals(QuoteStep.CONTACT, nextStep);
        assertEquals("Cylinder is leaking", state.getProblemDetailsSummary());
        assertEquals(QuoteStep.CONTACT, state.getCurrentStep());
        assertEquals(true, service.isComplete(state, "hot-water-cylinder"));
    }

    @Test
    void updateBoilerType_shouldReturnBoilerMakeForBoilerServiceAndGasSafety() {
        QuoteSessionState state = new QuoteSessionState();
        state.setGasSafetyServiceType(GasSafetyServiceType.BOILER_SERVICE);

        QuoteStep nextStep = service.updateBoilerType(state, BoilerType.COMBI, "gas-safety-certificate");

        assertEquals(QuoteStep.BOILER_MAKE, nextStep);
        assertEquals(BoilerType.COMBI, state.getBoilerType());
        assertEquals(QuoteStep.BOILER_MAKE, state.getCurrentStep());
    }

    @Test
    void updateBoilerMake_shouldReturnOwnershipForBoilerServiceAndGasSafety() {
        QuoteSessionState state = new QuoteSessionState();
        state.setGasSafetyServiceType(GasSafetyServiceType.BOILER_SERVICE);
        state.setBoilerType(BoilerType.COMBI);

        QuoteStep nextStep = service.updateBoilerMake(state, BoilerMake.VAILLANT, "gas-safety-certificate");

        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, nextStep);
        assertEquals(BoilerMake.VAILLANT, state.getBoilerMake());
        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, state.getCurrentStep());
    }

    @Test
    void updateBoilerType_shouldSkipBoilerPositionForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerType(state, BoilerType.COMBI, "boiler-repair");

        assertEquals(QuoteStep.BOILER_MAKE, nextStep);
        assertEquals(BoilerType.COMBI, state.getBoilerType());
    }

    @Test
    void updateBoilerType_shouldSkipBoilerConversionForHeatOnlyBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerType(state, BoilerType.HEAT_ONLY, "boiler-repair");

        assertEquals(QuoteStep.BOILER_MAKE, nextStep);
        assertEquals(BoilerType.HEAT_ONLY, state.getBoilerType());
    }

    @Test
    void updateBoilerMake_shouldReturnBoilerAgeForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerMake(state, BoilerMake.VAILLANT, "boiler-repair");

        assertEquals(QuoteStep.BOILER_AGE, nextStep);
        assertEquals(BoilerMake.VAILLANT, state.getBoilerMake());
    }

    @Test
    void updateBoilerAge_shouldReturnRepairProblemForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerAge(state, BoilerAge.TWO_TO_FIVE_YEARS, "boiler-repair");

        assertEquals(QuoteStep.REPAIR_PROBLEM, nextStep);
        assertEquals(BoilerAge.TWO_TO_FIVE_YEARS, state.getBoilerAge());
        assertEquals("2-5 years", state.getBoilerAgeSummary());
        assertEquals(QuoteStep.REPAIR_PROBLEM, state.getCurrentStep());
    }

    @Test
    void updateBoilerPosition_shouldSetBoilerPositionAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerPosition(state, BoilerPosition.WALL_MOUNTED);

        assertEquals(QuoteStep.BOILER_LOCATION, nextStep);
        assertEquals(BoilerPosition.WALL_MOUNTED, state.getBoilerPosition());
    }

    @Test
    void updateBoilerLocation_shouldSetBoilerLocationAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerLocation(state, BoilerLocation.KITCHEN);

        assertEquals(QuoteStep.BOILER_FLOOR_LEVEL, nextStep);
        assertEquals(BoilerLocation.KITCHEN, state.getBoilerLocation());
    }

    @Test
    void updateBoilerLocation_shouldSkipFloorLevelForLoft() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerLocation(state, BoilerLocation.LOFT_OR_ATTIC);

        assertEquals(QuoteStep.BOILER_CONDITION, nextStep);
        assertEquals(BoilerLocation.LOFT_OR_ATTIC, state.getBoilerLocation());
        assertNull(state.getBoilerFloorLevel());
        assertEquals(QuoteStep.BOILER_CONDITION, state.getCurrentStep());
    }

    @Test
    void canAccessStep_shouldSkipFloorLevelAndAllowConditionForLoft() {
        QuoteSessionState state = new QuoteSessionState();
        state.setBoilerLocation(BoilerLocation.LOFT_OR_ATTIC);

        assertFalse(service.canAccessStep(state, QuoteStep.BOILER_FLOOR_LEVEL));
        assertTrue(service.canAccessStep(state, QuoteStep.BOILER_CONDITION));
    }

    @Test
    void updateBoilerLocation_shouldSkipStraightToRepairProblemForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerLocation(state, BoilerLocation.KITCHEN, "boiler-repair");

        assertEquals(QuoteStep.REPAIR_PROBLEM, nextStep);
        assertEquals(BoilerLocation.KITCHEN, state.getBoilerLocation());
        assertNull(state.getBoilerFloorLevel());
    }

    @Test
    void updateBoilerFloorLevel_shouldSetBoilerFloorLevelAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerFloorLevel(state, BoilerFloorLevel.BASEMENT);

        assertEquals(QuoteStep.BOILER_CONDITION, nextStep);
        assertEquals(BoilerFloorLevel.BASEMENT, state.getBoilerFloorLevel());
    }

    @Test
    void updateBoilerCondition_shouldSetBoilerConditionAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerCondition(state, com.kgboilers.model.boilerinstallation.enums.BoilerCondition.NOT_WORKING);

        assertEquals(QuoteStep.RELOCATION, nextStep);
        assertEquals(com.kgboilers.model.boilerinstallation.enums.BoilerCondition.NOT_WORKING, state.getBoilerCondition());
    }

    @Test
    void updateBedrooms_shouldRejectNullBedrooms() {
        QuoteSessionState state = new QuoteSessionState();

        UnsupportedBedroomsException ex = assertThrows(
                UnsupportedBedroomsException.class,
                () -> service.updateBedrooms(state, null)
        );

        assertEquals("Unsupported bedrooms value: null", ex.getMessage());
    }

    @Test
    void updateBedrooms_shouldSetBedroomsAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBedrooms(state, Bedrooms.THREE);

        assertEquals(QuoteStep.BOILER_TYPE, nextStep);
        assertEquals(Bedrooms.THREE, state.getBedrooms());
    }

    @Test
    void updateRelocation_shouldGoToFlueType_whenRelocationIsNo() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateRelocation(state, Relocation.NO);

        assertEquals(QuoteStep.FLUE_TYPE, nextStep);
        assertEquals(Relocation.NO, state.getRelocation());
        assertEquals(QuoteStep.FLUE_TYPE, state.getCurrentStep());
    }

    @Test
    void updateRelocation_shouldGoToRelocationDistance_whenRelocationIsYes() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateRelocation(state, Relocation.YES);

        assertEquals(QuoteStep.RELOCATION_DISTANCE, nextStep);
        assertEquals(Relocation.YES, state.getRelocation());
        assertEquals(QuoteStep.RELOCATION_DISTANCE, state.getCurrentStep());
    }

    @Test
    void updateRelocationDistance_shouldGoToFlueType() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateRelocationDistance(state, RelocationDistance.TWO_TO_THREE);

        assertEquals(QuoteStep.FLUE_TYPE, nextStep);
        assertEquals(RelocationDistance.TWO_TO_THREE, state.getRelocationDistance());
        assertEquals(QuoteStep.FLUE_TYPE, state.getCurrentStep());
    }

    @Test
    void updateFlueType_shouldSetVerticalFlueTypeAndReturnFlueLength() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateFlueType(state, FlueType.VERTICAL, VerticalFlueType.FLAT_ROOF);

        assertEquals(QuoteStep.FLUE_LENGTH, nextStep);
        assertEquals(FlueType.VERTICAL, state.getFlueType());
        assertEquals(VerticalFlueType.FLAT_ROOF, state.getVerticalFlueType());
        assertEquals("Vertical flue (Flat roof)", state.getFlueSummary());
        assertEquals(QuoteStep.FLUE_LENGTH, state.getCurrentStep());
    }

    @Test
    void updateFlueType_shouldReturnFlueShape_forHorizontalFlue() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateFlueType(state, FlueType.HORIZONTAL, null);

        assertEquals(QuoteStep.FLUE_SHAPE, nextStep);
        assertEquals(FlueType.HORIZONTAL, state.getFlueType());
        assertNull(state.getHorizontalFlueShape());
        assertEquals(QuoteStep.FLUE_SHAPE, state.getCurrentStep());
    }

    @Test
    void updateHorizontalFlueShape_shouldSetShapeAndReturnFlueLength() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateHorizontalFlueShape(state, HorizontalFlueShape.ROUND);

        assertEquals(QuoteStep.FLUE_LENGTH, nextStep);
        assertEquals(HorizontalFlueShape.ROUND, state.getHorizontalFlueShape());
        assertEquals("Round", state.getHorizontalFlueShapeSummary());
        assertEquals(QuoteStep.FLUE_LENGTH, state.getCurrentStep());
    }

    @Test
    void updateFlueLength_shouldSetFlueLengthAndReturnFluePosition_forHorizontalFlue() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFlueType(FlueType.HORIZONTAL);

        QuoteStep nextStep = service.updateFlueLength(state, FlueLength.TWO_TO_THREE);

        assertEquals(QuoteStep.FLUE_POSITION, nextStep);
        assertEquals(FlueLength.TWO_TO_THREE, state.getFlueLength());
        assertEquals("2-3 metres", state.getFlueLengthSummary());
        assertEquals(QuoteStep.FLUE_POSITION, state.getCurrentStep());
    }

    @Test
    void updateFlueLength_shouldSetFlueLengthAndReturnRadiatorCount_forVerticalFlue() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFlueType(FlueType.VERTICAL);
        state.setVerticalFlueType(VerticalFlueType.FLAT_ROOF);

        QuoteStep nextStep = service.updateFlueLength(state, FlueLength.TWO_TO_THREE);

        assertEquals(QuoteStep.RADIATOR_COUNT, nextStep);
        assertEquals(FlueLength.TWO_TO_THREE, state.getFlueLength());
        assertEquals(QuoteStep.RADIATOR_COUNT, state.getCurrentStep());
    }

    @Test
    void updateFlueLength_shouldReturnSlopedRoofPosition_forVerticalSlopedRoof() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFlueType(FlueType.VERTICAL);
        state.setVerticalFlueType(VerticalFlueType.SLOPED_ROOF);

        QuoteStep nextStep = service.updateFlueLength(state, FlueLength.TWO_TO_THREE);

        assertEquals(QuoteStep.SLOPED_ROOF_POSITION, nextStep);
        assertEquals(FlueLength.TWO_TO_THREE, state.getFlueLength());
        assertEquals(QuoteStep.SLOPED_ROOF_POSITION, state.getCurrentStep());
    }

    @Test
    void updateSlopedRoofPosition_shouldSetPositionAndReturnRadiatorCount() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateSlopedRoofPosition(state, SlopedRoofPosition.HIGHEST_TWO_THIRDS);

        assertEquals(QuoteStep.RADIATOR_COUNT, nextStep);
        assertEquals(SlopedRoofPosition.HIGHEST_TWO_THIRDS, state.getSlopedRoofPosition());
        assertEquals("Upper half of the roof", state.getSlopedRoofPositionSummary());
    }

    @Test
    void updateFluePosition_shouldSetFluePositionAndReturnFlueClearance_forHorizontalFlue() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFlueType(FlueType.HORIZONTAL);

        QuoteStep nextStep = service.updateFluePosition(state, FluePosition.UNDER_STRUCTURE);

        assertEquals(QuoteStep.FLUE_CLEARANCE, nextStep);
        assertEquals(FluePosition.UNDER_STRUCTURE, state.getFluePosition());
        assertEquals("Under balcony or structure", state.getFluePositionSummary());
        assertEquals(QuoteStep.FLUE_CLEARANCE, state.getCurrentStep());
    }

    @Test
    void updateFlueClearance_shouldSetFlueClearanceAndReturnFluePropertyDistance() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateFlueClearance(state, FlueClearance.THIRTY_CM_OR_MORE);

        assertEquals(QuoteStep.FLUE_PROPERTY_DISTANCE, nextStep);
        assertEquals(FlueClearance.THIRTY_CM_OR_MORE, state.getFlueClearance());
        assertEquals("30 cm or more from an opening", state.getFlueClearanceSummary());
        assertEquals(QuoteStep.FLUE_PROPERTY_DISTANCE, state.getCurrentStep());
    }

    @Test
    void updateFluePropertyDistance_shouldSetFluePropertyDistanceAndReturnRadiatorCount() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateFluePropertyDistance(state, FluePropertyDistance.MORE_THAN_ONE_METRE);

        assertEquals(QuoteStep.RADIATOR_COUNT, nextStep);
        assertEquals(FluePropertyDistance.MORE_THAN_ONE_METRE, state.getFluePropertyDistance());
        assertEquals("More than 1 metre", state.getFluePropertyDistanceSummary());
        assertEquals(QuoteStep.RADIATOR_COUNT, state.getCurrentStep());
    }

    @Test
    void updateRadiatorCount_shouldSetRadiatorCountAndReturnBathShowerCount() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateRadiatorCount(state, RadiatorCount.TEN_TO_THIRTEEN);

        assertEquals(QuoteStep.BATH_SHOWER_COUNT, nextStep);
        assertEquals(RadiatorCount.TEN_TO_THIRTEEN, state.getRadiatorCount());
        assertEquals("10-13 radiators", state.getRadiatorCountSummary());
        assertEquals(QuoteStep.BATH_SHOWER_COUNT, state.getCurrentStep());
    }

    @Test
    void updateRadiatorCount_shouldReturnRepairProblem_forBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateRadiatorCount(state, RadiatorCount.SIX_TO_NINE, "boiler-repair");

        assertEquals(QuoteStep.REPAIR_PROBLEM, nextStep);
        assertEquals(RadiatorCount.SIX_TO_NINE, state.getRadiatorCount());
        assertEquals(QuoteStep.REPAIR_PROBLEM, state.getCurrentStep());
    }

    @Test
    void updatePowerFlush_shouldReturnRepairProblem_forBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updatePowerFlush(state, PowerFlushStatus.YES_DONE, "boiler-repair");

        assertEquals(QuoteStep.MAGNETIC_FILTER, nextStep);
        assertEquals(PowerFlushStatus.YES_DONE, state.getPowerFlushStatus());
        assertEquals("Yes, it was done", state.getPowerFlushSummary());
        assertEquals(QuoteStep.MAGNETIC_FILTER, state.getCurrentStep());
    }

    @Test
    void updateMagneticFilter_shouldReturnRepairProblem_forBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateMagneticFilter(state, MagneticFilterStatus.YES_HAS, "boiler-repair");

        assertEquals(QuoteStep.REPAIR_PROBLEM, nextStep);
        assertEquals(MagneticFilterStatus.YES_HAS, state.getMagneticFilterStatus());
        assertEquals("Yes, it has one", state.getMagneticFilterSummary());
        assertEquals(QuoteStep.REPAIR_PROBLEM, state.getCurrentStep());
    }

    @Test
    void updateRepairProblem_shouldSetProblemAndReturnFaultCode() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateRepairProblem(state, RepairProblem.HEATING_AND_HOT_WATER, "boiler-repair");

        assertEquals(QuoteStep.FAULT_CODE_DISPLAY, nextStep);
        assertEquals(RepairProblem.HEATING_AND_HOT_WATER, state.getRepairProblem());
        assertEquals("Heating & hot water", state.getRepairProblemSummary());
        assertEquals(QuoteStep.FAULT_CODE_DISPLAY, state.getCurrentStep());
    }

    @Test
    void updateBoilerPressure_shouldReturnFaultCodeStepForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerPressure(state, BoilerPressureStatus.YES_DROPPED_OR_DROPPING, "boiler-repair");

        assertEquals(QuoteStep.FAULT_CODE_DISPLAY, nextStep);
        assertEquals(BoilerPressureStatus.YES_DROPPED_OR_DROPPING, state.getBoilerPressureStatus());
        assertEquals("Yes, it has dropped or is dropping", state.getBoilerPressureSummary());
        assertEquals(QuoteStep.FAULT_CODE_DISPLAY, state.getCurrentStep());
    }

    @Test
    void updateFaultCodeDisplay_shouldReturnSummaryForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateFaultCodeDisplay(state, FaultCodeDisplayStatus.YES_SHOWING, "boiler-repair");

        assertEquals(QuoteStep.FAULT_CODE_DETAILS, nextStep);
        assertEquals(FaultCodeDisplayStatus.YES_SHOWING, state.getFaultCodeDisplayStatus());
        assertEquals("Yes, there is a fault code, message or signal", state.getFaultCodeDisplaySummary());
        assertEquals(QuoteStep.FAULT_CODE_DETAILS, state.getCurrentStep());
    }

    @Test
    void updateFaultCodeDisplay_shouldReturnSummaryForBoilerRepair_whenNothingIsShowing() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateFaultCodeDisplay(state, FaultCodeDisplayStatus.NO_NOT_SHOWING, "boiler-repair");

        assertEquals(QuoteStep.SUMMARY, nextStep);
        assertEquals(FaultCodeDisplayStatus.NO_NOT_SHOWING, state.getFaultCodeDisplayStatus());
        assertEquals(QuoteStep.SUMMARY, state.getCurrentStep());
        assertEquals("", state.getFaultCodeDetailsSummary());
    }

    @Test
    void updateFaultCodeDetails_shouldReturnSummaryForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();
        state.setFaultCodeDisplayStatus(FaultCodeDisplayStatus.YES_SHOWING);

        QuoteStep nextStep = service.updateFaultCodeDetails(state, "F22 low pressure warning", "boiler-repair");

        assertEquals(QuoteStep.SUMMARY, nextStep);
        assertEquals("F22 low pressure warning", state.getFaultCodeDetailsSummary());
        assertEquals(QuoteStep.SUMMARY, state.getCurrentStep());
    }

    @Test
    void updateBathShowerCount_shouldSetBathShowerCountAndReturnSummary() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBathShowerCount(state, BathShowerCount.THREE);

        assertEquals(QuoteStep.SUMMARY, nextStep);
        assertEquals(BathShowerCount.THREE, state.getBathShowerCount());
        assertEquals("3", state.getBathShowerCountSummary());
        assertEquals(QuoteStep.SUMMARY, state.getCurrentStep());
    }
}
