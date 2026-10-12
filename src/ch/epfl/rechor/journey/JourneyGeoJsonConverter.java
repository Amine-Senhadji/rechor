package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.*;

/**
 * Converter for journeys to GeoJSON format.
 * Builds a GeoJSON LineString representation of all journey stops,
 * rounding coordinates to the specified precision.
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public class JourneyGeoJsonConverter {
    /** Number of decimal places to round each coordinate to. */
    private static final int COORDINATE_PRECISION = 5;
    /** Multiplicative factor used for rounding coordinates to the specified precision. */
    private static final double ROUNDING_FACTOR = Math.pow(10, COORDINATE_PRECISION);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private JourneyGeoJsonConverter(){}

    /**
     * Converts a Journey into a GeoJSON LineString object.
     *
     * @param journey the journey to convert
     * @return a Json.JObject representing the GeoJSON LineString of the journey
     */
    public static Json toGeoJson(Journey journey) {
        List<Stop> stops = extractStops(journey);

        // Convert each stop to a GeoJSON coordinate [longitude, latitude] with rounding
        List<Json> coordinates = new ArrayList<>();
        for (Stop stop : stops) {
            double longitude = Math.round(stop.longitude() * ROUNDING_FACTOR) / ROUNDING_FACTOR;
            double latitude = Math.round(stop.latitude() * ROUNDING_FACTOR) / ROUNDING_FACTOR;
            coordinates.add(new Json.JArray(List.of(
                    new Json.JNumber(longitude),
                    new Json.JNumber(latitude)
            )));
        }
        // Construct the GeoJSON LineString object
        Map<String, Json> map = new LinkedHashMap<>(2);
        map.put("type", new Json.JString("LineString"));
        map.put("coordinates", new Json.JArray(coordinates));

        return new Json.JObject(map);
    }

    /**
     * Extracts all stops (departure, intermediate, arrival) from the journey legs.
     *
     * @param journey the journey containing legs
     * @return list of stops in order, including intermediate stops
     */
    private static List<Stop> extractStops(Journey journey) {
        List<Stop> stops = new ArrayList<>();
        for (Journey.Leg leg : journey.legs()) {
            stops.add(leg.depStop());
            for (Journey.Leg.IntermediateStop intermediateStop : leg.intermediateStops()) {
                stops.add(intermediateStop.stop());
            }
            stops.add(leg.arrStop());
        }
        return removeConsecutiveDuplicateStops(stops);
    }

    /**
     * Removes consecutive duplicate stops from a list.
     *
     * @param stops list of stops potentially containing consecutive duplicates
     * @return a new list without consecutive duplicates
     */
    private static List<Stop> removeConsecutiveDuplicateStops(List<Stop> stops) {
        List<Stop> uniqueStops = new ArrayList<>();
        for (Stop stop : stops) {
            int size = uniqueStops.size();
            if (size == 0 || !uniqueStops.get(size - 1).equals(stop)) {
                uniqueStops.add(stop);
            }
        }
        return uniqueStops;
    }
}
