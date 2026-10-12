package ch.epfl.rechor.journey;

import java.util.List;
/**
 * Enum representing different types of vehicles used in a journey.
 *  @author Amine Lotfi Senhadji
 *  @author Nadine Jegham
 */
public enum Vehicle {
    TRAM,
    METRO,
    TRAIN,
    BUS,
    FERRY,
    AERIAL_LIFT,
    FUNICULAR;
    /**
     * A list containing all vehicle types.
     */
    public static final List<Vehicle> ALL=List.of(values());

}
