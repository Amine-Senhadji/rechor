package ch.epfl.rechor.timetable;
/** @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public interface Stations extends Indexed{
    /**
     * Returns the name of the station at the specified index.
     * @param id The index of the station (must be in the range).
     * @return The name of the station as a String.
     * @throws IndexOutOfBoundsException If id is negative or greater than or equal
     *         to the size of the station collection.
     */
    String name (int id);
    /**
     * Returns the longitude of the station at the specified index in degrees.
     * @param id The index of the station (must be in the range).
     * @return The longitude of the station in degrees.
     * @throws IndexOutOfBoundsException If id is negative or greater than or equal
     *         to the size of the station collection.
     */
    double longitude(int id);
    /**
     * Returns the latitude of the station at the specified index in degrees.
     *
     * @param id The index of the station (must be in the range).
     * @return The latitude of the station in degrees.
     * @throws IndexOutOfBoundsException If id is negative or greater than or equal
     *         to the size of the station collection .
     */
    double latitude (int id);

}
