package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.exception.boilerinstallationquote.UnsupportedBedroomsException;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import com.kgboilers.model.boilerinstallation.enums.BathShowerCount;
import com.kgboilers.model.boilerinstallation.enums.BoilerFloorLevel;
import com.kgboilers.model.boilerinstallation.enums.BoilerLocation;
import com.kgboilers.model.boilerinstallation.enums.BoilerMake;
import com.kgboilers.model.boilerinstallation.enums.BoilerPosition;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallation.enums.FlueClearance;
import com.kgboilers.model.boilerinstallation.enums.FluePropertyDistance;
import com.kgboilers.model.boilerinstallation.enums.FlueType;
import com.kgboilers.model.boilerinstallation.enums.FlueLength;
import com.kgboilers.model.boilerinstallation.enums.FluePosition;
import com.kgboilers.model.boilerinstallation.enums.FuelType;
import com.kgboilers.model.boilerinstallation.enums.OwnershipType;
import com.kgboilers.model.boilerinstallation.enums.PropertyType;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallation.enums.RadiatorCount;
import com.kgboilers.model.boilerinstallation.enums.Relocation;
import com.kgboilers.model.boilerinstallation.enums.RelocationDistance;
import com.kgboilers.model.boilerinstallation.enums.SlopedRoofPosition;
import com.kgboilers.model.boilerinstallation.enums.VerticalFlueType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        assertEquals(QuoteStep.PROPERTY_OWNERSHIP, nextStep);
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
    void updateBoilerType_shouldSetBoilerTypeAndReturnNextStep() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerType(state, BoilerType.COMBI);

        assertEquals(QuoteStep.BOILER_POSITION, nextStep);
        assertEquals(BoilerType.COMBI, state.getBoilerType());
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
    void updateBoilerMake_shouldReturnBoilerLocationForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerMake(state, BoilerMake.VAILLANT, "boiler-repair");

        assertEquals(QuoteStep.BOILER_LOCATION, nextStep);
        assertEquals(BoilerMake.VAILLANT, state.getBoilerMake());
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
    void updateBoilerLocation_shouldSkipBoilerFloorLevelForBoilerRepair() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBoilerLocation(state, BoilerLocation.KITCHEN, "boiler-repair");

        assertEquals(QuoteStep.RADIATOR_COUNT, nextStep);
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
    void updateFlueType_shouldSetFlueTypeAndReturnSummary() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateFlueType(state, FlueType.VERTICAL, VerticalFlueType.FLAT_ROOF);

        assertEquals(QuoteStep.FLUE_LENGTH, nextStep);
        assertEquals(FlueType.VERTICAL, state.getFlueType());
        assertEquals(VerticalFlueType.FLAT_ROOF, state.getVerticalFlueType());
        assertEquals("Vertical flue (Flat roof)", state.getFlueSummary());
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
    void updateBathShowerCount_shouldSetBathShowerCountAndReturnSummary() {
        QuoteSessionState state = new QuoteSessionState();

        QuoteStep nextStep = service.updateBathShowerCount(state, BathShowerCount.THREE);

        assertEquals(QuoteStep.SUMMARY, nextStep);
        assertEquals(BathShowerCount.THREE, state.getBathShowerCount());
        assertEquals("3", state.getBathShowerCountSummary());
        assertEquals(QuoteStep.SUMMARY, state.getCurrentStep());
    }
}
