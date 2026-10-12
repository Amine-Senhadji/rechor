package ch.epfl.rechor;
/**
 * Utility class for checking preconditions.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
 public final class Preconditions {
    private Preconditions(){
        // Private constructor to prevent instantiation
    }
    /**
     * Checks if a condition is true, otherwise throws an IllegalArgumentException.
     * @param shouldBeTrue the condition that should be true
     */
    public static void checkArgument(boolean shouldBeTrue){
        if (!shouldBeTrue){
            throw new IllegalArgumentException();
        }
    }
}
