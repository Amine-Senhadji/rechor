package ch.epfl.rechor.timetable;

import java.util.NoSuchElementException;
/** @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */

public interface Transfers extends Indexed{
    /**
     * Returns the index of the departure station for the transfer at the specified index.
     *
     * @param id The index of the transfer.
     * @return The index of the departure station.
     * @throws IndexOutOfBoundsException if the provided index is invalid
     */
    int depStationId(int id);
    /**
     * Returns the duration in minutes of the transfer at the specified index.
     *
     * @param id The index of the transfer (must be in the range).
     * @return The duration of the transfer in minutes.
     * @throws IndexOutOfBoundsException if the provided index is invalid
     */
    int minutes (int id);
    /**
     * Returns the packed interval of transfer indexes that arrive at the specified station.
     * @param stationId The index of the arrival station.
     * @return A packed integer representing the interval of transfer indices.
     * @throws IllegalArgumentException if the stationId is invalid
     */
    int arrivingAt(int stationId);
    /**
     * Returns the duration in minutes of the transfer between the specified departure
     * and arrival stations.
     * @param depStationId The index of the departure station.
     * @param arrStationId The index of the arrival station.
     * @return The duration of the transfer in minutes.
     * @throws IllegalArgumentException if either station ID is invalid.
     * @throws NoSuchElementException if no transfer exists between the given stations.
     */
    int minutesBetween(int depStationId, int arrStationId);

}
