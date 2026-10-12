package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
/**
 * Represents a memory-mapped structure for storing and accessing connections efficiently.
 * This class provides direct access to packed connection data stored in a ByteBuffer.
 *  @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class BufferedConnections implements Connections {
    private static final int DEP_STOP_ID = 0;
    private static final int DEP_MINUTES = 1;
    private static final int ARR_STOP_ID = 2;
    private static final int ARR_MINUTES = 3;
    private static final int TRIP_POS_ID = 4;


    // Defines the structure of the connection records stored in memory
    private static final Structure CONNECTIONS_STRUCTURE = new Structure(
            Structure.field(DEP_STOP_ID, Structure.FieldType.U16),
            Structure.field(DEP_MINUTES, Structure.FieldType.U16),
            Structure.field(ARR_STOP_ID, Structure.FieldType.U16),
            Structure.field(ARR_MINUTES, Structure.FieldType.U16),
            Structure.field(TRIP_POS_ID, Structure.FieldType.S32)

    );

    private final StructuredBuffer buffer;// Main buffer for connection data
    private final IntBuffer succBuffer;// Buffer for storing successor connection indices

    /**
     * Constructs a BufferedConnections instance using the provided ByteBuffers.
     *
     * @param buffer      The ByteBuffer containing connection data.
     * @param succBuffer  The ByteBuffer containing successor connection indices.
     * @throws IllegalArgumentException if either buffer is null or improperly sized.
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer){
        this.buffer=new StructuredBuffer(CONNECTIONS_STRUCTURE,buffer);
        this.succBuffer=succBuffer.asIntBuffer();
    }

    /**
     * Retrieves the departure stop ID for the given connection.
     *
     * @param id The connection ID.
     * @return The departure stop ID.
     */
    @Override
    public int depStopId(int id) {
        return buffer.getU16(DEP_STOP_ID,id);
    }

    /**
     * Retrieves the departure time in minutes for the given connection.
     *
     * @param id The connection ID.
     * @return The departure time in minutes.
     */
    @Override
    public int depMins(int id) {
        return buffer.getU16(DEP_MINUTES,id);
    }

    /**
     * Retrieves the arrival stop ID for the given connection.
     *
     * @param id The connection ID.
     * @return The arrival stop ID.
     */
    @Override
    public int arrStopId(int id) {
        return buffer.getU16(ARR_STOP_ID,id);
    }

    /**
     * Retrieves the arrival time in minutes for the given connection.
     *
     * @param id The connection ID.
     * @return The arrival time in minutes.
     */
    @Override
    public int arrMins(int id) {
        return buffer.getU16(ARR_MINUTES,id);
    }

    /**
     * Retrieves the trip ID associated with the given connection.
     *
     * @param id The connection ID.
     * @return The trip ID (extracted from the upper 24 bits).
     */
    @Override
    public int tripId(int id) {
        int tripPosValue = buffer.getS32(TRIP_POS_ID, id);
        return Bits32_24_8.unpack24(tripPosValue);
    }

    /**
     * Retrieves the trip position within the route for the given connection.
     *
     * @param id The connection ID.
     * @return The trip position (extracted from the lower 8 bits).
     */
    @Override
    public int tripPos(int id) {
        int tripPosValue = buffer.getS32(TRIP_POS_ID, id);
        return Bits32_24_8.unpack8(tripPosValue);
    }

    /**
     * Retrieves the next connection ID in the sequence for a given connection.
     *
     * @param id The connection ID.
     * @return The next connection ID.
     */
    @Override
    public int nextConnectionId(int id) {
        return succBuffer.get(id);
    }

    /**
     * Returns the total number of stored connections.
     *
     * @return The number of connections.
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
