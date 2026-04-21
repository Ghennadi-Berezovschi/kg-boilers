package com.kgboilers.exception.boilerinstallationquote;

import com.kgboilers.controller.centralheatingquote.CentralHeatingQuoteApiController;
import com.kgboilers.controller.boilerinstallationquote.QuoteWizardApiController;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.exception.ExternalServiceException;
import com.kgboilers.exception.PostcodeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
        QuoteWizardApiController.class,
        CentralHeatingQuoteApiController.class
})
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String UNSUPPORTED_FUEL_MESSAGE =
            "Sorry, we currently do not support this type of fuel.";

    @ExceptionHandler(PostcodeNotFoundException.class)
    public ResponseEntity<QuoteResponseDto> handlePostcodeNotFound(PostcodeNotFoundException ex) {
        log.warn("Invalid postcode submitted");
        return badRequest("INVALID_POSTCODE", "Please enter a valid postcode");
    }

    @ExceptionHandler(OutOfAreaException.class)
    public ResponseEntity<QuoteResponseDto> handleOutOfArea(OutOfAreaException ex) {
        log.warn("Address is out of service area");
        return forbidden("OUT_OF_AREA", "Sorry, we do not cover your area");
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<QuoteResponseDto> handleExternalService(ExternalServiceException ex) {
        log.error("External service error", ex);
        return serviceUnavailable(
                "SERVICE_UNAVAILABLE",
                "Service temporarily unavailable. Please try again later"
        );
    }

    @ExceptionHandler(UnsupportedFuelException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedFuel(UnsupportedFuelException ex) {
        log.warn("Unsupported fuel submitted");
        return badRequest("UNSUPPORTED_FUEL", UNSUPPORTED_FUEL_MESSAGE);
    }

    @ExceptionHandler(UnsupportedOwnershipException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedOwnership(UnsupportedOwnershipException ex) {
        log.warn("Unsupported ownership submitted");
        return badRequest("UNSUPPORTED_OWNERSHIP", "Unsupported ownership type");
    }

    @ExceptionHandler(UnsupportedPropertyTypeException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedPropertyType(UnsupportedPropertyTypeException ex) {
        log.warn("Unsupported property type submitted");
        return badRequest("UNSUPPORTED_PROPERTY_TYPE", "Unsupported property type");
    }

    @ExceptionHandler(UnsupportedBoilerFloorLevelException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedBoilerFloorLevel(UnsupportedBoilerFloorLevelException ex) {
        log.warn("Unsupported boiler floor level submitted");
        return badRequest("UNSUPPORTED_BOILER_FLOOR_LEVEL", "Unsupported boiler floor level");
    }

    @ExceptionHandler(UnsupportedBoilerLocationException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedBoilerLocation(UnsupportedBoilerLocationException ex) {
        log.warn("Unsupported boiler location submitted");
        return badRequest("UNSUPPORTED_BOILER_LOCATION", "Unsupported boiler location");
    }

    @ExceptionHandler(UnsupportedBedroomsException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedBedrooms(UnsupportedBedroomsException ex) {
        log.warn("Unsupported bedrooms submitted");
        return badRequest("UNSUPPORTED_BEDROOMS", "Unsupported bedrooms value");
    }

    @ExceptionHandler(UnsupportedBoilerTypeException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedBoilerType(UnsupportedBoilerTypeException ex) {
        log.warn("Unsupported boiler type submitted");
        return badRequest("UNSUPPORTED_BOILER_TYPE", "Unsupported boiler type");
    }

    @ExceptionHandler(UnsupportedBoilerMakeException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedBoilerMake(UnsupportedBoilerMakeException ex) {
        log.warn("Unsupported boiler make submitted");
        return badRequest("UNSUPPORTED_BOILER_MAKE", "Unsupported boiler make");
    }

    @ExceptionHandler(UnsupportedBoilerAgeException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedBoilerAge(UnsupportedBoilerAgeException ex) {
        log.warn("Unsupported boiler age submitted");
        return badRequest("UNSUPPORTED_BOILER_AGE", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedPowerFlushException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedPowerFlush(UnsupportedPowerFlushException ex) {
        log.warn("Unsupported power flush answer submitted");
        return badRequest("UNSUPPORTED_POWER_FLUSH", "Unsupported power flush answer");
    }

    @ExceptionHandler(UnsupportedMagneticFilterException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedMagneticFilter(UnsupportedMagneticFilterException ex) {
        log.warn("Unsupported magnetic filter answer submitted");
        return badRequest("UNSUPPORTED_MAGNETIC_FILTER", "Unsupported magnetic filter answer");
    }

    @ExceptionHandler(UnsupportedBoilerPressureException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedBoilerPressure(UnsupportedBoilerPressureException ex) {
        log.warn("Unsupported boiler pressure answer submitted");
        return badRequest("UNSUPPORTED_BOILER_PRESSURE", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedFaultCodeDisplayException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedFaultCodeDisplay(UnsupportedFaultCodeDisplayException ex) {
        log.warn("Unsupported fault code answer submitted");
        return badRequest("UNSUPPORTED_FAULT_CODE", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedFaultCodeDetailsException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedFaultCodeDetails(UnsupportedFaultCodeDetailsException ex) {
        log.warn("Unsupported fault code details submitted");
        return badRequest("UNSUPPORTED_FAULT_CODE_DETAILS", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedTrvValveException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedTrvValve(UnsupportedTrvValveException ex) {
        log.warn("Unsupported TRV valve answer submitted");
        return badRequest("UNSUPPORTED_TRV_VALVE", "Unsupported TRV valve answer");
    }

    @ExceptionHandler(UnsupportedRadiatorIssueException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedRadiatorIssue(UnsupportedRadiatorIssueException ex) {
        log.warn("Unsupported radiator issue submitted");
        return badRequest("UNSUPPORTED_RADIATOR_ISSUE", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedRepairProblemException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedRepairProblem(UnsupportedRepairProblemException ex) {
        log.warn("Unsupported repair problem submitted");
        return badRequest("UNSUPPORTED_REPAIR_PROBLEM", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedRadiatorSpecificationException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedRadiatorSpecification(UnsupportedRadiatorSpecificationException ex) {
        log.warn("Unsupported radiator specification submitted");
        return badRequest("UNSUPPORTED_RADIATOR_SPECIFICATION", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedSlopedRoofPositionException.class)
    public ResponseEntity<QuoteResponseDto> handleUnsupportedSlopedRoofPosition(UnsupportedSlopedRoofPositionException ex) {
        log.warn("Unsupported sloped roof position submitted");
        return badRequest("UNSUPPORTED_SLOPED_ROOF_POSITION", "Unsupported roof position");
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<QuoteResponseDto> handleValidation(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        log.warn("Validation error on quote request");

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");

        return badRequest("INVALID_INPUT", message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<QuoteResponseDto> handleUnknown(Exception ex) {
        log.error("Unexpected error", ex);
        return internalError("INTERNAL_SERVER_ERROR", "An unexpected error occurred.");
    }

    private ResponseEntity<QuoteResponseDto> badRequest(String code, String message) {
        return build(HttpStatus.BAD_REQUEST, code, message);
    }

    private ResponseEntity<QuoteResponseDto> forbidden(String code, String message) {
        return build(HttpStatus.FORBIDDEN, code, message);
    }

    private ResponseEntity<QuoteResponseDto> serviceUnavailable(String code, String message) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, code, message);
    }

    private ResponseEntity<QuoteResponseDto> internalError(String code, String message) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, code, message);
    }

    private ResponseEntity<QuoteResponseDto> build(HttpStatus status,
                                                   String errorCode,
                                                   String message) {

        QuoteResponseDto body = QuoteResponseDto.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
