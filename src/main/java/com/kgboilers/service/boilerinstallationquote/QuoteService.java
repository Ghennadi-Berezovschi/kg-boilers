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

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        return startQuote(postcode, "boiler-installation");
    }

    public QuoteStep startQuote(String postcode, String service) {

        String normalizedPostcode = normalizePostcode(postcode);
        String normalizedService = normalizeService(service);
        List<String> engineerPostcodes = getEngineerPostcodes(normalizedService);

        log.info("Starting quote distance validation");

        Coordinates clientCoords = postcodeService.getCoordinates(normalizedPostcode);
        double distance = engineerPostcodes.stream()
                .map(postcodeValue -> postcodeService.getCoordinates(postcodeValue))
                .mapToDouble(engineerCoords -> distanceService.calculateMiles(clientCoords, engineerCoords))
                .min()
                .orElseThrow(() -> new ExternalServiceException(
                        "Server configuration error",
                        new RuntimeException("Missing engineer postcode")
                ));

        log.info("Calculated distance: {} miles", distance);

        validateDistance(distance, normalizedService);

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

    private List<String> getEngineerPostcodes(String service) {
        Map<String, List<String>> servicePostcodes = locationProperties.getServicePostcodes();
        if (servicePostcodes != null) {
            List<String> configuredServicePostcodes = servicePostcodes.get(service);
            if (configuredServicePostcodes != null) {
                List<String> normalizedServicePostcodes = configuredServicePostcodes.stream()
                        .filter(StringUtils::hasText)
                        .map(value -> value.trim().toUpperCase())
                        .toList();
                if (!normalizedServicePostcodes.isEmpty()) {
                    return normalizedServicePostcodes;
                }
            }
        }

        List<String> configuredPostcodes = locationProperties.getPostcodes();
        if (configuredPostcodes != null) {
            List<String> normalizedPostcodes = configuredPostcodes.stream()
                    .filter(StringUtils::hasText)
                    .map(value -> value.trim().toUpperCase())
                    .toList();
            if (!normalizedPostcodes.isEmpty()) {
                return normalizedPostcodes;
            }
        }

        String postcode = locationProperties.getPostcode();
        if (StringUtils.hasText(postcode)) {
            return List.of(postcode.trim().toUpperCase());
        }

        log.error("Company postcode is not configured");
        throw new ExternalServiceException(
                "Server configuration error",
                new RuntimeException("Missing engineer postcode")
        );
    }

    private void validateDistance(double distance, String service) {
        double maxDistance = getMaxDistanceMiles(service);

        if (distance > maxDistance) {
            log.warn("Address is out of service area at {} miles", distance);
            throw new OutOfAreaException();
        }
    }

    private double getMaxDistanceMiles(String service) {
        Map<String, Double> serviceLimits = locationProperties.getServiceMaxDistanceMiles();
        if (serviceLimits == null) {
            return locationProperties.getMaxDistanceMiles();
        }

        return serviceLimits.getOrDefault(service, locationProperties.getMaxDistanceMiles());
    }

    private String normalizeService(String service) {
        if (!StringUtils.hasText(service)) {
            return "boiler-installation";
        }

        return service.trim().toLowerCase(Locale.ROOT);
    }
}
