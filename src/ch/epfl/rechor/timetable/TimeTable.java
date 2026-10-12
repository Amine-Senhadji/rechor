package ch.epfl.rechor.timetable;


import java.time.LocalDate;
/** @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */

public interface TimeTable {
    Stations stations();
    StationAliases stationAliases();
    Platforms platforms();
    Routes routes();
    Transfers transfers();
    Trips tripsFor(LocalDate date);
    Connections connectionsFor(LocalDate date);
    /**
     * Determines if the given stop ID corresponds to a station.
     * A stop ID is a station ID if it is in the range.
     * Note: This method assumes non-negative stop IDs; negative values are considered invalid.
     *
     * @param stopId The stop ID to check.
     * @return True if the stop ID represents a station,False otherwise.
     */
    default boolean isStationId(int stopId){
        return stopId < stations().size();
    }
    /**
     * Determines if the given stop ID corresponds to a platform.
     * A stop ID is a platform ID if it is in the range.
     * Note: This method assumes the stop ID is valid; invalid values may lead to
     * unexpected behavior in dependent methods.
     *
     * @param stopId The stop ID to check.
     * @return True if the stop ID represents a platform, False otherwise.
     */
    default boolean isPlatformId(int stopId){
        int platformStart = stations().size();
        return stopId >= platformStart ;
    }
    /**
     * Returns the station ID associated with the given stop ID.
     * @param stopId The stop ID to convert (either a station or platform ID).
     * @return The corresponding station ID.
     * @throws IndexOutOfBoundsException If the stop ID is a platform ID and the computed
     *         index exceeds the size of the platform's collection.
     */
    default int stationId(int stopId){
        return (isStationId(stopId))?stopId: platforms().stationId(stopId - stations().size());
    }
    /**
     * Returns the name of the platform associated with the given stop ID, if it is a platform.
     * @param stopId The stop ID to query.
     * @return The name of the platform if the stop ID is a platform ID, or null
     *         if it is a station ID or invalid.
     * @throws IndexOutOfBoundsException If the stop ID is a platform ID and the computed
     *         index exceeds the size of the platform's collection.
     */
    default String platformName(int stopId){
        if(isPlatformId(stopId)){
            return platforms().name(stopId - stations().size());
        }
        return null;
    }



}
