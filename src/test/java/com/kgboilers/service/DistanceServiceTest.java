package com.kgboilers.service;

import com.kgboilers.model.Coordinates;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DistanceServiceTest {

    private final DistanceService distanceService = new DistanceService();

    @Test
    void testCalculateMiles_LondonToManchester() {
        // Координаты Лондона: 51.5074, -0.1278
        // Координаты Манчестера: 53.4808, -2.2426
        // Реальное расстояние по формуле Гаверсинуса: ~163 мили
        Coordinates london = new Coordinates(51.5074, -0.1278);
        Coordinates manchester = new Coordinates(53.4808, -2.2426);

        double distance = distanceService.calculateMiles(london, manchester);

        // Ожидаем примерно 162.8 мили по формуле Гаверсинуса
        assertEquals(162.8, distance, 0.1);
    }

    @Test
    void testCalculateMiles_SamePoint() {
        Coordinates point = new Coordinates(51.5074, -0.1278);
        double distance = distanceService.calculateMiles(point, point);
        assertEquals(0.0, distance, 0.001);
    }
}
