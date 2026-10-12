package ch.epfl.rechor.timetable;
/** @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */

public interface Platforms extends Indexed{
    /**
     * Returns the name of the platform corresponding to the given index.
     * The platform name may be a number (e.g., "70") or a letter (e.g., "A").
     * It can also be an empty string if the platform has no specific name.
     * @param id The index of the platform.
     * @return The name of the platform.
     * @throws IndexOutOfBoundsException if the provided index is invalid.
     *
     */
    String name(int id);
    /**
     * Returns the index of the station to which the platform corresponds.
     *
     * @param id The index of the platform.
     * @return The index of the corresponding station.
     * @throws IndexOutOfBoundsException if the provided index is invalid.
     */
    int stationId(int id);
}
