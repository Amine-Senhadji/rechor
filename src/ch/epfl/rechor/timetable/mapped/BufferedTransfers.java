package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * BufferedTransfers implements the Transfers interface using a flat, memory‐mapped representation.
 * Each transfer record is stored in a StructuredBuffer according to TRANSFERS_STRUCTURE.
 * Each record consists of:
 *  U16 for the departure station ID
 *  U16 for the arrival station ID
 *  U8 for the transfer duration (in minutes)
 * This class precomputes, in a two-pass process, an array of packed intervals (using PackedRange)
 * that associates each arrival station with the indices of transfers arriving at that station.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class BufferedTransfers implements Transfers {
    private static final int DEP_STATION_ID = 0;
    private static final int ARR_STATION_ID = 1;
    private static final int TRANSFER_MINUTES = 2;

    /**
     * The structure for transfers, which comprises:
     *   Field 0: U16 for departure station ID
     *   Field 1: U16 for arrival station ID
     *   Field 2: U8 for transfer duration (minutes)
     */
    private static final Structure TRANSFERS_STRUCTURE = new Structure(
            Structure.field(DEP_STATION_ID, Structure.FieldType.U16),
            Structure.field(ARR_STATION_ID, Structure.FieldType.U16),
            Structure.field(TRANSFER_MINUTES, Structure.FieldType.U8)
    );

    private final StructuredBuffer buffer;
    /**
     * Precomputed table: For each station index i, stores the packed interval (via PackedRange)
     * of indices in the buffer corresponding to transfers arriving at station i.
     */
    private final int[] arrivalIntervals;

    /**
     * Constructs a BufferedTransfers instance from the given ByteBuffer.
     * The ByteBuffer contains flat transfer records formatted according to TRANSFERS_STRUCTURE.
     * The constructor performs two passes:
     *   The first pass computes the maximum station ID (considering both departure and arrival IDs).
     *   The second pass groups consecutive transfers (grouped by arrival station) and packs their indices using PackedRange.
     *
     * @param buffer the ByteBuffer containing the flat transfer records
     */
    public BufferedTransfers(ByteBuffer buffer) {
        this.buffer = new StructuredBuffer(TRANSFERS_STRUCTURE, buffer);
        int numTransfers = this.buffer.size();

        // First pass: determine the maximum station ID among both departure and arrival fields.
        int maxArrStationId = -1;
        for (int i = 0; i < numTransfers; i++) {
            int arrId = this.buffer.getU16(ARR_STATION_ID, i);
            int depId = this.buffer.getU16(DEP_STATION_ID, i);
            maxArrStationId = Math.max(maxArrStationId, Math.max(arrId, depId));
        }

        // Allocate the arrivalIntervals array for station indices [0, maxArrStationId]
        arrivalIntervals = new int[maxArrStationId + 1];
        // Initialize the array with zeros (0 indicates no transfers).
        Arrays.fill(arrivalIntervals, 0);

        // Second pass: for each group of consecutive transfers (grouped by arrival station),
        // compute the interval [startIndex, endIndex) and store it using PackedRange.
        int currentIndex = 0;
        while (currentIndex < numTransfers) {
            int currentArrId = this.buffer.getU16(ARR_STATION_ID, currentIndex);
            int startIndex = currentIndex;
            // Traverse all transfers with the same arrival station.
            while (currentIndex < numTransfers &&
                    this.buffer.getU16(ARR_STATION_ID, currentIndex) == currentArrId) {
                currentIndex++;
            }
            int endIndex = currentIndex; // exclusive
            int packedInterval = PackedRange.pack(startIndex, endIndex);
            arrivalIntervals[currentArrId] = packedInterval;
        }
    }

    /**
     * Returns the departure station ID for the transfer record at the specified index.
     *
     * @param id the index of the transfer record
     * @return the departure station ID (as an unsigned 16-bit integer)
     */
    @Override
    public int depStationId(int id) {
        return buffer.getU16(DEP_STATION_ID, id);
    }

    /**
     * Returns the transfer duration (in minutes) for the transfer record at the specified index.
     *
     * @param id the index of the transfer record
     * @return the transfer duration in minutes
     */
    @Override
    public int minutes(int id) {
        return buffer.getU8(TRANSFER_MINUTES, id);
    }

    /**
     * Returns the packed interval (using PackedRange) of transfer indices for transfers arriving at the given station.
     *
     * @param stationId the arrival station index
     * @return the packed interval representing the indices in the buffer for transfers arriving at that station
     */
    @Override
    public int arrivingAt(int stationId) {
        return arrivalIntervals[stationId];
    }

    /**
     * Returns the transfer duration (in minutes) for a transfer from the given departure station to the specified arrival station.
     * The method searches within the precomputed interval for transfers arriving at the specified station.
     * If no matching transfer is found, a NoSuchElementException is thrown.
     *
     * @param depStationId the departure station ID
     * @param arrStationId the arrival station ID
     * @return the transfer duration in minutes for the matching transfer record
     * @throws NoSuchElementException if no transfer from the specified departure station is found at the given arrival station
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        int interval = arrivalIntervals[arrStationId];
        int start = PackedRange.startInclusive(interval);
        int end = PackedRange.endExclusive(interval);

        for (int i = start; i < end; i++) {
            int depId = buffer.getU16(DEP_STATION_ID, i);
            if (depId == depStationId) {
                return minutes(i);
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the total number of transfer records in the buffer.
     *
     * @return the number of transfer records
     */
    @Override
    public int size() {
        return buffer.size();
    }
}