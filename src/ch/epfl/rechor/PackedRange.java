package ch.epfl.rechor;
/**
 * A utility class for manipulating packed integer ranges in a 32-bit int.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class PackedRange {
    // Private constructor to prevent instantiation
    private PackedRange(){}
    /**
     * Packs a range defined by start (inclusive) and end (exclusive) into an int.
     * The start is stored in the upper 24 bits, and the length in the lower 8 bits.
     * @param startInclusive The starting integer (inclusive, must be non-negative)
     * @param endExclusive The ending integer (exclusive)
     * @return A packed int representing the range
     * @throws IllegalArgumentException if start is negative, or length is invalid (≤ 0 or > 255)
     */
    public static int pack (int startInclusive, int endExclusive){
        int length=endExclusive- startInclusive;
        return Bits32_24_8.pack(startInclusive,length);
    }
    /**
     * Extracts the length of the packed range.
     * @param interval The packed range
     * @return The length of the range (number of integers)
     */
    public static int length (int interval){
        return Bits32_24_8.unpack8(interval);
    }
    /**
     * Extracts the start (inclusive) of the packed range.
     * @param interval The packed range
     * @return The starting integer (inclusive)
     */
    public static int startInclusive(int interval){
        return Bits32_24_8.unpack24(interval);
    }
    /**
     * Calculates the end (exclusive) of the packed range.
     * @param interval The packed range
     * @return The ending integer (exclusive)
     */
    public static int endExclusive(int interval){
        return startInclusive(interval)+length(interval);
    }
}