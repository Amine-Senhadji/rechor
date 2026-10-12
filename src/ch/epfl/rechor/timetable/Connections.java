package ch.epfl.rechor.timetable;
/**
 * Represents indexed connections in a public transportation schedule.
 * Connections must be ordered by decreasing departure time.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public interface Connections extends Indexed{
    /**
     * Returns the departure stop index of the connection with the given index.
     * @param id The index of the connection.
     * @return The departure stop index.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    int depStopId(int id);
    /**
     * Returns the departure time of the connection with the given index,
     * expressed in minutes after midnight.
     * @param id The index of the connection.
     * @return The departure time in minutes after midnight.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    int depMins(int id);
    /**
     * Returns the arrival stop index of the connection with the given index.
     * @param id The index of the connection.
     * @return The arrival stop index.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    int arrStopId(int id);
    /**
     * Returns the arrival time of the connection with the given index,
     * expressed in minutes after midnight.
     * @param id The index of the connection.
     * @return The arrival time in minutes after midnight.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    int arrMins(int id);
    /**
     * Returns the trip index to which the connection with the given index belongs.
     * @param id The index of the connection.
     * @return The trip index.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    int tripId(int id);
    /**
     * Returns the position of the connection within its trip.
     * The first connection of a trip always has position 0.
     * @param id The index of the connection.
     * @return The position of the connection in its trip.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    int tripPos(int id);
    /**
     * Returns the index of the next connection in the same trip,
     * or the index of the first connection in the trip if the given connection is the last one.
     * @param id The index of the connection.
     * @return The index of the next connection.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    int nextConnectionId(int id);



}
