package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
/**
 * A utility class for manipulating packed trip optimization criteria in a 64-bit long.
 *  @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class PackedCriteria {
    //private constructor to prevent instantiation
    private PackedCriteria(){}
    // Constants for time boundaries and bit manipulation
    private static final int ORIGIN     =-240;
    private static final int MAX_MINUTES=2880;
    private static final int MIN_MINUTES=-240;
    private static final int MAX_CHANGES=127;
    private static final int COMPLEMENT =4095;

    private static final int MASK_12_BITS = 0xFFF;
    private static final int MASK_7_BITS = 0x7F;
    private static final long MASK_32_BITS = 0xFFFFFFFFL;

    private static final long DEP_BITS = (long) MASK_12_BITS << 51;
    private static final long CHANGE_BITS = (long) MASK_7_BITS << 32;
    private static final long PAYLOAD_BITS = MASK_32_BITS;

    /**
     * Packs arrival time, changes, and payload into a long without departure time.
     * @param arrMins Arrival time in minutes since midnight
     * @param changes Number of changes (0-127)
     * @param payload 32-bit payload
     * @return A long with packed criteria
     * @throws IllegalArgumentException if arrMins or changes are out of bounds
     *
     */
    public static long pack (int arrMins, int changes, int payload){
        Preconditions.checkArgument((arrMins>=MIN_MINUTES)&&(arrMins<MAX_MINUTES));
        Preconditions.checkArgument((changes>=0)&&(changes<=MAX_CHANGES));

        final int arrFromOrigin = toStoredArrivalMins(arrMins); // translate to origin-based minutes

        return ((long) arrFromOrigin << 39) | ((long) changes << 32) | Integer.toUnsignedLong(payload);
    }
    /**
     * Checks if the criteria include a departure time.
     * @param criteria Packed criteria
     * @return true if departure time is present (bits 51-62 != 0)
     */
    public static boolean hasDepMins(long criteria){
        return (criteria & DEP_BITS)!=0;
    }
    /**
     * Extracts departure time in minutes since midnight.
     * @param criteria Packed criteria
     * @return Departure time
     * @throws IllegalArgumentException if no departure time is present
     */
    public static int depMins(long criteria){
        Preconditions.checkArgument(hasDepMins(criteria));
        return fromStoredDepartureMins((int) ((criteria >> 51) & MASK_12_BITS));
    }
    /**
     * Extracts arrival time in minutes since midnight.
     * @param criteria Packed criteria
     * @return Arrival time
     */
    public static int arrMins(long criteria){
        return (int) ((criteria >> 39) & MASK_12_BITS) + ORIGIN;
    }
    /**
     * Extracts the number of changes.
     * @param criteria Packed criteria
     * @return Number of changes (0-127)
     */
    public static int changes (long criteria){
        return (int) ((criteria >> 32) & MASK_7_BITS);
    }
    /**
     * Extracts the payload.
     * @param criteria Packed criteria
     * @return 32-bit payload as an int
     */
    public static int payload (long criteria){
        return (int)(criteria & PAYLOAD_BITS);
    }
    /**
     * Checks if criteria1 dominates or equals criteria2.
     * Dominance: dep1 >= dep2, arr1 <= arr2, chg1 <= chg2 (if departure present).
     * @param criteria1 First packed criteria
     * @param criteria2 Second packed criteria
     * @return true if criteria1 dominates or equals criteria2
     * @throws IllegalArgumentException if one has departure time and the other doesn't
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2){
        Preconditions.checkArgument((hasDepMins(criteria1)) == (hasDepMins(criteria2)));
        int dep1 = hasDepMins(criteria1)? depMins(criteria1):0;
        int dep2 = hasDepMins(criteria2)? depMins(criteria2):0;
        int arr1=arrMins(criteria1);
        int arr2= arrMins(criteria2);
        int change1= changes(criteria1);
        int change2= changes(criteria2);
        return (dep1 >= dep2 || !hasDepMins(criteria1)) && arr1 <= arr2 && change1 <= change2;
    }
    /**
     * Returns criteria without departure time.
     * @param criteria Packed criteria
     * @return Criteria with bits 51-62 set to 0
     */
    public static long withoutDepMins(long criteria){
        return hasDepMins(criteria) ? criteria & ~DEP_BITS : criteria;
    }
    /**
     * Returns criteria with a new departure time.
     * @param criteria Packed criteria
     * @param depMins1 Departure time in minutes since midnight
     * @return Updated criteria
     * @throws IllegalArgumentException if depMins1 is out of bounds
     */
    public static long withDepMins(long criteria, int depMins1){
        Preconditions.checkArgument(depMins1>=MIN_MINUTES && depMins1< MAX_MINUTES);
        int depMins2= toStoredDepartureMins(depMins1);

        return (criteria & ~DEP_BITS) | ((long) depMins2 << 51);
    }
    /**
     * Returns criteria with one additional change.
     * @param criteria Packed criteria
     * @return Updated criteria
     * @throws IllegalArgumentException if changes exceed max
     */
    public static  long withAdditionalChange(long criteria){
        int numberChanges= changes(criteria)+1;
        Preconditions.checkArgument(numberChanges<=MAX_CHANGES);
        return ((criteria & ~CHANGE_BITS) | ((long) numberChanges << 32));
    }
    /**
     * Returns criteria with a new payload.
     * @param criteria Packed criteria
     * @param payload New payload value
     * @return Updated criteria
     */
    public static long withPayload(long criteria,int payload){
        return ((criteria & ~PAYLOAD_BITS) | Integer.toUnsignedLong(payload));
    }

    private static int toStoredArrivalMins(int arrMins) {
        return arrMins - ORIGIN;
    }

    private static int toStoredDepartureMins(int depMins) {
        return COMPLEMENT - depMins + ORIGIN;
    }

    private static int fromStoredDepartureMins(int stored) {
        return COMPLEMENT + ORIGIN - stored;
    }
}
