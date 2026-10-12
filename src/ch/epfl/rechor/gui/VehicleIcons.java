package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;


/**
 * Utility class for retrieving and caching icons associated with transport vehicles.
 * <p>
 * Each icon is loaded from a PNG file named after the vehicle's enum name
 * (e.g., "BUS.png" for Vehicle.BUS), and cached in memory to avoid reloading.
 * </p>
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class VehicleIcons {
    /** Cache mapping each Vehicle to its loaded icon Image to prevent redundant loading. */
    private static final Map<Vehicle, Image> myMap = new EnumMap<>(Vehicle.class);
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private VehicleIcons(){}


    /**
     * Retrieves the icon image corresponding to the given vehicle.
     * <p>
     * If the image is not already cached, it is loaded from a resource named {@code vehicleName.png}.
     * </p>
     *
     * @param vehicle the vehicle whose icon should be retrieved
     * @return the image representing the vehicle
     */
    public static Image iconFor(Vehicle vehicle){
        return myMap.computeIfAbsent(vehicle,v-> new Image(v.name()+".png"));
    }
}
