package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * BufferedPlatforms provides access to a flattened table of platform data.
 * It implements the Platforms interface by using a StructuredBuffer to access
 * platform fields stored in a ByteBuffer.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class BufferedPlatforms implements Platforms {

    // Field indices for the flattened platforms data structure.
    private static final int NAME_ID = 0;
    private static final int STATION_ID = 1;

    // Definition of the structure for platforms: name index (U16) and station index (U16).
    private static final Structure PLATFORMS_STRUCTURE = new Structure(
            Structure.field(NAME_ID, Structure.FieldType.U16),
            Structure.field(STATION_ID, Structure.FieldType.U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedPlatforms instance with the given string table and ByteBuffer.
     *
     * @param stringTable the table of strings used to resolve platform names
     * @param buffer      the ByteBuffer containing the flattened platform data
     */
    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(PLATFORMS_STRUCTURE, buffer);
    }

    /**
     * Returns the name of the platform at the given index.
     *
     * @param id the platform index
     * @return the platform name
     */
    @Override
    public String name(int id) {
        int nameIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * Returns the station index associated with the platform at the given index.
     *
     * @param id the platform index
     * @return the station index
     */
    @Override
    public int stationId(int id) {
        return buffer.getU16(STATION_ID, id);
    }

    /**
     * Returns the number of platform entries in the dataset.
     *
     * @return the size of the platform data
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
