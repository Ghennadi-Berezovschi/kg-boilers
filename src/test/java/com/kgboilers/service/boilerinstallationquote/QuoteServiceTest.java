package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.LocationProperties;
import com.kgboilers.exception.boilerinstallationquote.OutOfAreaException;
import com.kgboilers.model.Coordinates;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.service.DistanceService;
import com.kgboilers.service.PostcodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuoteServiceTest {

    private PostcodeService postcodeService;
    private DistanceService distanceService;
    private LocationProperties locationProperties;
    private QuoteService quoteService;

    @BeforeEach
    void setUp() {
        postcodeService = mock(PostcodeService.class);
        distanceService = mock(DistanceService.class);
        locationProperties = mock(LocationProperties.class);

        when(locationProperties.getPostcode()).thenReturn("E16 4JJ");
        when(locationProperties.getMaxDistanceMiles()).thenReturn(70.0);

        quoteService = new QuoteService(locationProperties, postcodeService, distanceService);
    }

    @Test
    void shouldReturnNextStep_whenWithinDistance() {
        Coordinates coords = new Coordinates(51.5, 0.0);

        when(postcodeService.getCoordinates(anyString())).thenReturn(coords);
        when(distanceService.calculateMiles(any(), any())).thenReturn(10.0);

        QuoteStep step = quoteService.startQuote("E16 4JJ");

        assertEquals(QuoteStep.FUEL_TYPE, step);
    }

    @Test
    void shouldThrowOutOfAreaException_whenTooFar() {
        Coordinates coords = new Coordinates(51.5, 0.0);

        when(postcodeService.getCoordinates(anyString())).thenReturn(coords);
        when(distanceService.calculateMiles(any(), any())).thenReturn(100.0);

        assertThrows(OutOfAreaException.class,
                () -> quoteService.startQuote("ZZ1 1ZZ"));
    }
}
