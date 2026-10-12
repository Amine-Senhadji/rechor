package ch.epfl.rechor.timetable.mapped;
import ch.epfl.rechor.timetable.Trips;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * BufferedTrips is an implementation of the Trips interface using a flat data representation.
 * <p>
 * Trip data is stored in a StructuredBuffer based on a defined Structure.
 * This class provides methods to access route IDs and destination names for each trip.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class BufferedTrips implements Trips {

    // Field indices for the trips structure.
    private static final int ROUTE_ID_FIELD = 0;
    private static final int DESTINATION_FIELD = 1;

    // Define the structure for trips:
    // - Field 0 (ROUTE_ID_FIELD): an unsigned 16-bit integer representing the route ID.
    // - Field 1 (DESTINATION_FIELD): an unsigned 16-bit integer representing the destination index in the string table.
    private static final Structure TRIPS_STRUCTURE = new Structure(
            Structure.field(ROUTE_ID_FIELD, Structure.FieldType.U16),
            Structure.field(DESTINATION_FIELD, Structure.FieldType.U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedTrips instance with the specified string table and data buffer.
     *
     * @param stringTable a list of strings used to map destination names
     * @param buffer      a ByteBuffer containing the flat trips data
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(TRIPS_STRUCTURE, buffer);
    }

    /**
     * Returns the number of trips contained in this buffer.
     *
     * @return the number of trips
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the route ID for the trip at the specified index.
     *
     * @param id the index of the trip
     * @return the route ID as an unsigned 16-bit integer
     */
    @Override
    public int routeId(int id) {
        return buffer.getU16(ROUTE_ID_FIELD, id);
    }

    /**
     * Returns the destination name for the trip at the specified index.
     *
     * @param id the index of the trip
     * @return the destination name corresponding to the destination index stored in the trip data
     */
    @Override
    public String destination(int id) {
        int destIndex = buffer.getU16(DESTINATION_FIELD, id);
        return stringTable.get(destIndex);
    }
}