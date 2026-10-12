package ch.epfl.rechor.timetable;
/** @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */

public interface Trips extends Indexed{
    /**
     * Returns the index of the route corresponding to the trip at the specified index.
     *
     * @param id The index of the trip (must be in the range).
     * @return The index of the route associated with the trip.
     * @throws IndexOutOfBoundsException if the provided index is invalid (i.e., negative or
     *         greater than or equal to the size).
     */
    int routeId(int id);
    /**
     * Returns the name of the final destination of the trip at the specified index.
     *
     * @param id The index of the trip (must be in the range).
     * @return The name of the final destination as a String.
     * @throws IndexOutOfBoundsException if the provided index is invalid (i.e., negative or
     *         greater than or equal to the size).
     */
    String destination(int id);

}
