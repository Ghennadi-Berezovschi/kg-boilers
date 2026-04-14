package com.kgboilers.service;

import com.kgboilers.model.Coordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DistanceService {

    private static final Logger log = LoggerFactory.getLogger(DistanceService.class);

    private static final double EARTH_RADIUS_MILES = 3958.8;

    public double calculateMiles(Coordinates a, Coordinates b) {
        double lat1 = Math.toRadians(a.getLat());
        double lon1 = Math.toRadians(a.getLon());
        double lat2 = Math.toRadians(b.getLat());
        double lon2 = Math.toRadians(b.getLon());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double haversine = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);

        double distanceMiles = 2 * EARTH_RADIUS_MILES * Math.asin(Math.sqrt(haversine));

        log.debug("Distance calculated using Haversine: {} miles", distanceMiles);

        return distanceMiles;
    }
}