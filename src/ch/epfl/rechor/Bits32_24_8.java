package ch.epfl.rechor;


/**
 * A utility class for packing and unpacking two values into a 32-bit int.
 *
 * <p>The 24-bit value is stored in the upper 24 bits, and the 8-bit value in the lower 8 bits.</p>
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class Bits32_24_8 {
    // Private constructor to prevent instantiation.
    private Bits32_24_8() {}

    /**
     * Packs a 24-bit value and an 8-bit value into a 32-bit int.
     *
     * @param bits24 the value to store in the upper 24 bits (0 to 2^24 - 1)
     * @param bits8  the value to store in the lower 8 bits (0 to 255)
     * @return a packed 32-bit int
     * @throws IllegalArgumentException if bits24 exceeds 24 bits or bits8 exceeds 8 bits
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits24 >> 24) == 0);
        Preconditions.checkArgument((bits8 >> 8) == 0);
        return (bits24 << 8) | (bits8 & 0xFF);
    }

    /**
     * Extracts the 24-bit value from the upper 24 bits of a packed int.
     *
     * @param bits32 the packed 32-bit int
     * @return the 24-bit value (upper 24 bits shifted to lower 24 bits)
     */
    public static int unpack24(int bits32) {
        return (bits32 >> 8) & 0xFFFFFF;
    }

    /**
     * Extracts the 8-bit value from the lower 8 bits of a packed int.
     *
     * @param bits32 the packed 32-bit int
     * @return the 8-bit value (lower 8 bits)
     */
    public static int unpack8(int bits32) {
        return bits32 & 0xFF;
    }
}
