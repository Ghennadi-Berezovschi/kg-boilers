package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.LocationProperties;
import com.kgboilers.exception.ExternalServiceException;
import com.kgboilers.exception.boilerinstallationquote.OutOfAreaException;
import com.kgboilers.model.Coordinates;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.service.DistanceService;
import com.kgboilers.service.PostcodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

    private final LocationProperties locationProperties;
    private final PostcodeService postcodeService;
    private final DistanceService distanceService;

    public QuoteService(LocationProperties locationProperties,
                        PostcodeService postcodeService,
                        DistanceService distanceService) {
        this.locationProperties = locationProperties;
        this.postcodeService = postcodeService;
        this.distanceService = distanceService;
    }

    public QuoteStep startQuote(String postcode) {

        String normalizedPostcode = normalizePostcode(postcode);
        String engineerPostcode = getEngineerPostcode();

        log.info("Starting quote distance validation");

        Coordinates clientCoords = postcodeService.getCoordinates(normalizedPostcode);
        Coordinates engineerCoords = postcodeService.getCoordinates(engineerPostcode);

        double distance = distanceService.calculateMiles(clientCoords, engineerCoords);

        log.info("Calculated distance: {} miles", distance);

        validateDistance(distance);

        log.info("Address is within service area");

        return QuoteStep.FUEL_TYPE;
    }

    // =========================
    // Private helpers (clean!)
    // =========================

    private String normalizePostcode(String postcode) {
        if (!StringUtils.hasText(postcode)) {
            log.warn("Empty postcode received");
            throw new IllegalArgumentException("POSTCODE_REQUIRED");
        }
        return postcode.trim().toUpperCase();
    }

    private String getEngineerPostcode() {
        String postcode = locationProperties.getPostcode();

        if (!StringUtils.hasText(postcode)) {
            log.error("Company postcode is not configured");
            throw new ExternalServiceException(
                    "Server configuration error",
                    new RuntimeException("Missing engineer postcode")
            );
        }

        return postcode.trim().toUpperCase();
    }

    private void validateDistance(double distance) {
        double maxDistance = locationProperties.getMaxDistanceMiles();

        if (distance > maxDistance) {
            log.warn("Address is out of service area at {} miles", distance);
            throw new OutOfAreaException();
        }
    }
}
