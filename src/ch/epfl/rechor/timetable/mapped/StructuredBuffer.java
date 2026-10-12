package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;
import java.nio.ByteBuffer;

/**
 * StructuredBuffer provides an abstraction for accessing a byte buffer that contains
 * flat-structured data as defined by a Structure instance.
 * Each element of the structured buffer corresponds to a record of fixed size, as defined
 * by the Structure.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public class StructuredBuffer {
    private final int totalSize;
    private final Structure structure;
    private final ByteBuffer buffer;

    /**
     * Constructs a new StructuredBuffer with the given structure and byte buffer.
     * The capacity of the buffer must be a multiple of the total size of the structure.
     * @param structure the Structure describing the layout of each element
     * @param buffer the ByteBuffer containing the data
     * @throws IllegalArgumentException if buffer.capacity() is not a multiple of structure.totalSize()
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.totalSize = buffer.capacity() / structure.totalSize();
        this.structure = structure;
        this.buffer = buffer;
    }

    /**
     * Returns the number of elements stored in this buffer.
     *
     * @return the number of elements
     */
    public int size() {
        return totalSize;
    }

    /**
     * Retrieves an unsigned 8-bit integer from the buffer.
     *
     * @param fieldIndex   the index of the field within the structure
     * @param elementIndex the index of the element in the buffer
     * @return the unsigned 8-bit integer as an int
     */
    public int getU8(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Byte.toUnsignedInt(buffer.get(offset));
    }

    /**
     * Retrieves an unsigned 16-bit integer from the buffer.
     *
     * @param fieldIndex   the index of the field within the structure
     * @param elementIndex the index of the element in the buffer
     * @return the unsigned 16-bit integer as an int
     */
    public int getU16(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    /**
     * Retrieves a signed 32-bit integer from the buffer.
     *
     * @param fieldIndex   the index of the field within the structure
     * @param elementIndex the index of the element in the buffer
     * @return the 32-bit signed integer
     */
    public int getS32(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return buffer.getInt(offset);
    }
}