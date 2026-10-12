package ch.epfl.rechor.timetable;
/** @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */

public interface StationAliases extends Indexed{
    /**
     * Returns the alternative name (alias) associated with the given index.
     * This allows different naming conventions or translations for stations.
     * @param id The index of the alias.
     * @return The alternative station name.
     * @throws IndexOutOfBoundsException if the provided index is invalid.
     */
    String alias(int id);/**
     * Returns the official station name corresponding to the alias at the given index.
     * This method allows retrieving the actual station name from an alternative naming system.
     *
     * @param id The index of the alias.
     * @return The corresponding station name.
     * @throws IndexOutOfBoundsException if the provided index is invalid.
     */
    String stationName(int id);

}
