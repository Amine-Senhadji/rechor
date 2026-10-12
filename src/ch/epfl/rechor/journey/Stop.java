package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

/**
 * Represents a stop in a journey, with a name, platform, and geographical coordinates.
 * <p>
 * A valid stop must have a non-null name and coordinates within specified ranges.
 * </p>
 *
 * @param name the name of the stop (must not be null)
 * @param platformName the name of the platform (nullable)
 * @param longitude the longitude of the stop (between -180 and 180)
 * @param latitude the latitude of the stop (between -90 and 90)
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 * @version 1.0
 * @since 1.0
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;

    /**
     * Constructor that ensures valid input values.
     *
     * @param name The name of the stop (must not be null).
     * @param platformName The name of the platform (can be null or empty).
     * @param longitude The longitude of the stop (must be between -180 and 180).
     * @param latitude The latitude of the stop (must be between -90 and 90).
     */
    public Stop {
        Objects.requireNonNull(name);

        // Validate longitude range
        Preconditions.checkArgument(
                longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE);

        // Validate latitude range
        Preconditions.checkArgument(
                latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE);

    }
}