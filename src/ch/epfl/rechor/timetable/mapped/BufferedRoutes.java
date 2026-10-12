package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * BufferedRoutes provides access to a flattened table of route data.
 * It implements the Routes interface by using a StructuredBuffer to access
 * route fields stored in a ByteBuffer.
 *  @author Amine Lotfi Senhadji
 *  @author Nadine Jegham
 */
public final class BufferedRoutes implements Routes {

    // Field indices for the flattened routes data structure.
    private static final int NAME_ID = 0;
    private static final int KIND = 1;

    // Definition of the structure for routes: name index (U16) and kind (U8)
    private static final Structure ROUTES_STRUCTURE = new Structure(
            Structure.field(NAME_ID, Structure.FieldType.U16),
            Structure.field(KIND, Structure.FieldType.U8)
    );

    // Cache the array of Vehicle values to avoid repeated calls to Vehicle.values()
    private static final Vehicle[] VEHICLE_VALUES = Vehicle.values();

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    /**
     * Constructs a BufferedRoutes instance with the given string table and ByteBuffer.
     *
     * @param stringTable the table of strings used to resolve route names
     * @param buffer      the ByteBuffer containing the flattened route data
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(ROUTES_STRUCTURE, buffer);
    }

    /**
     * Returns the vehicle type for the route at the given index.
     *
     * @param id the route index
     * @return the vehicle type of the route
     */
    @Override
    public Vehicle vehicle(int id) {
        final int typeIndex = buffer.getU8(KIND, id);
        return VEHICLE_VALUES[typeIndex];
    }

    /**
     * Returns the name of the route at the given index.
     *
     * @param id the route index
     * @return the route name
     */
    @Override
    public String name(int id) {
        final int nameIndex = buffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * Returns the number of routes.
     *
     * @return the number of routes in the dataset
     */
    @Override
    public int size() {
        return buffer.size();
    }
}