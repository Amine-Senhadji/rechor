package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * BufferedStations provides access to a flattened table of station data.
 * It implements the Stations interface and uses a StructuredBuffer to access
 * station fields stored in a ByteBuffer.
 *  @author Amine Lotfi Senhadji
 *  @author Nadine Jegham
 */
public final class BufferedStations implements Stations {

    // Conversion constant to convert raw integer coordinates to degrees.
    private static final double CONVERSION_CONSTANT = 360 * Math.scalb(1.0, -32);

    // Field indices for the flattened station data structure.
    private static final int STATION_NAME_ID = 0;
    private static final int LONGITUDE = 1;
    private static final int LATITUDE = 2;

    // Definition of the structure for station data: name index (U16), longitude (S32), and latitude (S32).
    private static final Structure STATION_STRUCTURE = new Structure(
            Structure.field(STATION_NAME_ID, Structure.FieldType.U16),
            Structure.field(LONGITUDE, Structure.FieldType.S32),
            Structure.field(LATITUDE, Structure.FieldType.S32)
    );

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedStations instance using the provided string table and ByteBuffer.
     *
     * @param stringTable the table of strings used to resolve station names
     * @param buffer      the ByteBuffer containing the flattened station data
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(STATION_STRUCTURE, buffer);
    }

    /**
     * Returns the number of stations in this dataset.
     *
     * @return the number of stations
     */
    @Override
    public int size() {
        return buffer.size();
    }

    /**
     * Returns the name of the station at the given index.
     *
     * @param id the station index
     * @return the name of the station
     */
    @Override
    public String name(int id) {
        int nameIndex = buffer.getU16(STATION_NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * Returns the longitude of the station at the given index, converted to degrees.
     *
     * @param id the station index
     * @return the longitude in degrees
     */
    @Override
    public double longitude(int id) {
        int rawLongitude = buffer.getS32(LONGITUDE, id);
        return rawLongitude * CONVERSION_CONSTANT;
    }

    /**
     * Returns the latitude of the station at the given index, converted to degrees.
     *
     * @param id the station index
     * @return the latitude in degrees
     */
    @Override
    public double latitude(int id) {
        int rawLatitude = buffer.getS32(LATITUDE, id);
        return rawLatitude * CONVERSION_CONSTANT;
    }

}
