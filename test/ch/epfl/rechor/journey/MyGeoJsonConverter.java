package ch.epfl.rechor.journey;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Test suite for the JourneyGeoJsonConverter class ensuring that
 * journey legs alternate as required by the Journey constructor.
 */
public class MyGeoJsonConverter {

    /**
     * Test a simple journey with a Foot leg then a Transport leg to ensure alternation.
     * This journey connects three stops.
     */
    @Test
    void testSimpleJourneyGeoJsonWithAlternateLegs() {
        // Create stops
        Stop A = new Stop("A", "Platform A", 6.62909, 46.51679);
        Stop B = new Stop("B", "Platform B", 6.83787, 46.54276);
        Stop C = new Stop("C", "Platform C", 6.91181, 46.69351);

        // Create time points
        LocalDateTime t1 = LocalDateTime.of(2023, 4, 11, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2023, 4, 11, 10, 10);
        LocalDateTime t3 = LocalDateTime.of(2023, 4, 11, 10, 15);
        LocalDateTime t4 = LocalDateTime.of(2023, 4, 11, 10, 25);

        // Leg 1: Foot leg from A to B.
        Journey.Leg.Foot leg1 = new Journey.Leg.Foot(A, t1, B, t2);
        // Leg 2: Transport leg from B to C (alternates with Foot).
        Journey.Leg.Transport leg2 = new Journey.Leg.Transport(
                B, t3,
                C, t4,
                List.of(), // No intermediate stops
                Vehicle.TRAIN,
                "Route1",
                "Destination"
        );

        Journey journey = new Journey(List.of(leg1, leg2));

        String geoJson = (JourneyGeoJsonConverter.toGeoJson(journey)).toString();
        String expected = "{\"type\":\"LineString\",\"coordinates\":[[6.62909,46.51679],[6.83787,46.54276],[6.91181,46.69351]]}";
        assertEquals(expected, geoJson);
    }

    /**
     * Test that duplicate consecutive stops are removed.
     * This test uses three legs that alternate types:
     * Leg 1: Foot from A to B.
     * Leg 2: Transport from B to B (transfer leg, duplicate stop).
     * Leg 3: Foot from B to C.
     * Expected output contains stops A, B, C (duplicate B omitted).
     */
    @Test
    void testDuplicateStopsRemovedWithAlternatingLegs() {
        // Create stops
        Stop A = new Stop("A", "", 6.62909, 46.51679);
        Stop B = new Stop("B", "", 6.83787, 46.54276);
        Stop C = new Stop("C", "", 6.91181, 46.69351);

        // Create time points
        LocalDateTime t1 = LocalDateTime.of(2023, 4, 11, 9, 0);
        LocalDateTime t2 = LocalDateTime.of(2023, 4, 11, 9, 10);
        LocalDateTime t3 = LocalDateTime.of(2023, 4, 11, 9, 15);
        LocalDateTime t4 = LocalDateTime.of(2023, 4, 11, 9, 20);
        LocalDateTime t5 = LocalDateTime.of(2023, 4, 11, 9, 25);
        LocalDateTime t6 = LocalDateTime.of(2023, 4, 11, 9, 30);

        // Leg 1: Foot leg from A to B.
        Journey.Leg.Foot leg1 = new Journey.Leg.Foot(A, t1, B, t2);
        // Leg 2: Transport leg from B to B (transfer; duplicate stop) -
        // Note: Although departure and arrival are equal, leg types alternate (Foot vs Transport).
        Journey.Leg.Transport leg2 = new Journey.Leg.Transport(
                B, t3,
                B, t4,
                List.of(),
                Vehicle.BUS,
                "RouteX",
                "DestinationX"
        );
        // Leg 3: Foot leg from B to C.
        Journey.Leg.Foot leg3 = new Journey.Leg.Foot(B, t5, C, t6);

        Journey journey = new Journey(List.of(leg1, leg2, leg3));
        String geoJson = (JourneyGeoJsonConverter.toGeoJson(journey)).toString();
        // Expected stops: A, B, C (duplicate B removed).
        String expected = "{\"type\":\"LineString\",\"coordinates\":[[6.62909,46.51679],[6.83787,46.54276],[6.91181,46.69351]]}";
        assertEquals(expected, geoJson);
    }

    /**
     * Test that coordinates are correctly rounded to 5 decimal places.
     */
    @Test
    void testRounding() {
        // Create stops with extra decimals.
        Stop A = new Stop("A", "", 6.12345678, 46.87654321);
        Stop B = new Stop("B", "", 7.98765432, 47.12345678);

        // Create time points.
        LocalDateTime t1 = LocalDateTime.of(2023, 4, 11, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2023, 4, 11, 8, 15);

        // Use one leg only (Foot leg) for simplicity.
        Journey.Leg.Foot leg = new Journey.Leg.Foot(A, t1, B, t2);
        Journey journey = new Journey(List.of(leg));

        String geoJson = (JourneyGeoJsonConverter.toGeoJson(journey)).toString();
        // Expected: A rounds to [6.12346,46.87654], B rounds to [7.98765,47.12346]
        String expected = "{\"type\":\"LineString\",\"coordinates\":[[6.12346,46.87654],[7.98765,47.12346]]}";
        assertEquals(expected, geoJson);
    }

    /**
     * Test a journey with a transport leg that includes intermediate stops.
     */
    @Test
    void testTransportWithIntermediateStops() {
        // Create stops.
        Stop dep = new Stop("Dep", "", 6.0, 46.0);
        Stop inter = new Stop("Inter", "", 6.5, 46.5);
        Stop arr = new Stop("Arr", "", 7.0, 47.0);

        // Set up times for the transport leg.
        LocalDateTime t1 = LocalDateTime.of(2023, 4, 11, 7, 0);
        LocalDateTime t2 = LocalDateTime.of(2023, 4, 11, 7, 15);
        LocalDateTime t3 = LocalDateTime.of(2023, 4, 11, 7, 30);
        LocalDateTime t4 = LocalDateTime.of(2023, 4, 11, 7, 45);

        // Create an intermediate stop record for the transport leg.
        Journey.Leg.IntermediateStop intermediateStop =
                new Journey.Leg.IntermediateStop(inter, t2, t3);

        // Create a transport leg with intermediate stops.
        // Since this is the only leg, there's no alternation issue.
        Journey.Leg.Transport leg = new Journey.Leg.Transport(
                dep, t1,
                arr, t4,
                List.of(intermediateStop),
                Vehicle.TRAIN,
                "Route1",
                "Destination"
        );
        Journey journey = new Journey(List.of(leg));

        String geoJson = (JourneyGeoJsonConverter.toGeoJson(journey)).toString();
        // Expected stops: dep, inter, arr.
        String expected = "{\"type\":\"LineString\",\"coordinates\":[[6.0,46.0],[6.5,46.5],[7.0,47.0]]}";
        assertEquals(expected, geoJson);
    }

    /**
     * Test that passing a null Journey causes a NullPointerException.
     */
    @Test
    void testNullJourney() {
        assertThrows(NullPointerException.class, () -> {
            JourneyGeoJsonConverter.toGeoJson(null);
        });
    }

    @Test
    void testRounding1() {
        // Create stops with more than 5 decimals
        Stop A = new Stop("A", "", 6.12345678, 46.87654321);
        Stop B = new Stop("B", "", 7.98765432, 47.12345678);

        // Create time points
        LocalDateTime t1 = LocalDateTime.of(2023, 4, 11, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2023, 4, 11, 8, 15);
        Journey.Leg.Foot leg = new Journey.Leg.Foot(A, t1, B, t2);
        Journey journey = new Journey(List.of(leg));

        String geoJson = (JourneyGeoJsonConverter.toGeoJson(journey)).toString();
        // Expected: A rounds to [6.12346,46.87654], B rounds to [7.98765,47.12346]
        String expected = "{\"type\":\"LineString\",\"coordinates\":[[6.12346,46.87654],[7.98765,47.12346]]}";
        assertEquals(expected, geoJson);
    }
}
