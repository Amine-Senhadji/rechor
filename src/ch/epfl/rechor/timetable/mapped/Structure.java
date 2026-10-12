package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

/**
 * Structure represents a layout of fields in a record. It provides methods to compute the
 * byte offset of a field within an array of structured elements.
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public class Structure {

    /**
     * Creates a new Field with the specified index and type.
     *
     * @param index the index of the field
     * @param type  the field type
     * @return a new Field instance
     */
    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    /**
     * FieldType enumerates the types of fields and their sizes in bytes.
     */
    public enum FieldType {
        U8(1),
        U16(2),
        S32(4);

        private final int size;

        FieldType(int size) {
            this.size = size;
        }

        /**
         * Returns the size in bytes of this field type.
         *
         * @return the size in bytes
         */
        public int getSize() {
            return size;
        }
    }

    /**
     * Field represents a field with an index and a type.
     *
     * @param index the index of the field
     * @param type  the field type
     */
    public record Field(int index, FieldType type) {
        public Field {
            if (type == null) {
                throw new NullPointerException();
            }
        }

        /**
         * Returns the type of this field.
         *
         * @return the field type
         */
        public FieldType getType() {
            return type;
        }
    }

    private final int totalSize;
    private final int[] fieldOffset;

    /**
     * Constructs a Structure from the given fields. Each field's index must match its
     * position in the array.
     *
     * @param fields the fields of the structure
     * @throws IllegalArgumentException if a field's index does not match its position
     */
    public Structure(Field... fields) {
        for (int i = 0; i < fields.length; i++) {
            Preconditions.checkArgument(fields[i].index() == i);
        }
        fieldOffset = new int[fields.length];
        int currentOffset = 0;
        for (int i = 0; i < fields.length; i++) {
            fieldOffset[i] = currentOffset;
            currentOffset += fields[i].getType().getSize();
        }
        totalSize = currentOffset;
    }

    /**
     * Returns the total size in bytes of the structure.
     *
     * @return the total size
     */
    public int totalSize() {
        return totalSize;
    }

    /**
     * Computes the byte offset for the specified field and element index.
     *
     * @param fieldIndex   the index of the field
     * @param elementIndex the index of the element in an array of structures
     * @return the computed byte offset
     */
    public int offset(int fieldIndex, int elementIndex) {
        return elementIndex * totalSize + fieldOffset[fieldIndex];
    }
}