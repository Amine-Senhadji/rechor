package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * BufferedStationAliases provides access to a flattened table of station alias data.
 * It implements the StationAliases interface by using a StructuredBuffer to access the alias fields stored in a ByteBuffer.
 *  @author Amine Lotfi Senhadji
 *  @author Nadine Jegham
 */
public final class BufferedStationAliases implements StationAliases {

    // Field indices for the alias data structure
    private static final int ALIAS_ID = 0;
    private static final int STATION_NAME_ID = 1;

    // Define the structure: first field is ALIAS_ID (U16) and second is STATION_NAME_ID (U16)
    private static final Structure ALIAS_STRUCTURE = new Structure(
            Structure.field(ALIAS_ID, Structure.FieldType.U16),
            Structure.field(STATION_NAME_ID, Structure.FieldType.U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedStationAliases instance with the given string table and ByteBuffer.
     *
     * @param stringTable the table of strings used to resolve station names and aliases
     * @param buffer      the ByteBuffer containing the flattened alias data
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(ALIAS_STRUCTURE, buffer);
    }

    /**
     * Returns the alias corresponding to the given index.
     *
     * @param id the index of the alias entry
     * @return the alias string
     */
    @Override
    public String alias(int id) {
        int aliasIndex = buffer.getU16(ALIAS_ID, id);
        return stringTable.get(aliasIndex);
    }

    /**
     * Returns the station name corresponding to the given alias entry index.
     *
     * @param id the index of the alias entry
     * @return the station name string
     */
    @Override
    public String stationName(int id) {
        int nameIndex = buffer.getU16(STATION_NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * Returns the number of alias entries available.
     *
     * @return the size of the alias data
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
