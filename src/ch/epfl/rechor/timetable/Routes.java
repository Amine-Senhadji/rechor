package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;
/** @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */

public interface Routes extends Indexed{
    /**
     * Returns the type of vehicle used on the route corresponding to the given index.
     *
     * @param id The index of the route.
     * @return The Vehicle type used on this route.
     * @throws IndexOutOfBoundsException if the provided index is invalid.
     */
    Vehicle vehicle(int id);
    /**
     * Returns the name of the route corresponding to the given index.
     * The route name could be a train line identifier (e.g., "IR 15") or a bus number.
     * @param id The index of the route.
     * @return The name of the route.
     * @throws IndexOutOfBoundsException if the provided index is invalid.
     */
    String name(int id);
}
