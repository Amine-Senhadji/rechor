package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
/**
 * Represents a journey composed of a sequence of legs.
 * A journey is validated upon creation to ensure that:
 * the list of legs is non-null and non-empty,
 * legs alternate between foot and transport legs,
 * the departure time of each leg is not before the arrival time of the previous leg, and
 * the departure stop of each leg equals the arrival stop of the previous leg.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public record Journey (List<Leg> legs){
    /**
     * Constructor that validates the journey structure.
     */
    public Journey{
        Objects.requireNonNull(legs);
        Preconditions.checkArgument(!legs.isEmpty());
        legs=List.copyOf(legs);

        // Ensure that foot legs are not consecutive
        for (int i = 1; i < legs.size(); i++) {

            Leg prev = legs.get(i - 1);
            Leg curr = legs.get(i);
            boolean prevIsFoot = prev instanceof Leg.Foot;
            boolean currIsFoot = curr instanceof Leg.Foot;

            Preconditions.checkArgument(prevIsFoot != currIsFoot);
            Preconditions.checkArgument(!curr.depTime().isBefore(prev.arrTime()));
            Preconditions.checkArgument(curr.depStop().equals(prev.arrStop()));
        }

    }
    /**
     * @return Departure stop of the journey.
     */
    public Stop depStop(){
        return legs.getFirst().depStop();
    }
    /**
     * @return Arrival stop of the journey.
     */
    public Stop arrStop(){
        return legs.getLast().arrStop();
    }
    /**
     * @return Departure time of the journey.
     */
    public LocalDateTime depTime(){
        return legs.getFirst().depTime();
    }
    /**
     * @return Arrival time of the journey.
     */
    public LocalDateTime arrTime(){
        return legs.getLast().arrTime();
    }
    /**
     * @return Total duration of the journey.
     */
    public Duration duration(){
        return Duration.between(depTime(),arrTime());
    }

    /**
     * Represents a leg of a journey, either on foot or by transport.
     */
    public sealed interface Leg {
        Stop depStop();

        Stop arrStop();

        LocalDateTime depTime();

        LocalDateTime arrTime();

        List<IntermediateStop> intermediateStops();
        /**
         * @return Duration of this leg.
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Represents an intermediate stop within a leg.
         */
        record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {
            public IntermediateStop {
                Objects.requireNonNull(stop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrTime);
                Preconditions.checkArgument(!depTime.isBefore(arrTime));
            }
        }
        /**
         * Represents a leg of the journey using public transport.
         */
        record Transport(Stop depStop,
                         LocalDateTime depTime,
                         Stop arrStop,
                         LocalDateTime arrTime,
                         List<IntermediateStop> intermediateStops,
                         Vehicle vehicle,
                         String route,
                         String destination)
                implements Leg {
            public Transport {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Objects.requireNonNull(vehicle);
                Objects.requireNonNull(route);
                Objects.requireNonNull(destination);
                // Check that the arrival time is not before the departure time
                Preconditions.checkArgument(!arrTime.isBefore(depTime));

                // Create an immutable copy of intermediate stops for immutability
                intermediateStops = List.copyOf(intermediateStops);
            }


        }

        /**
         * Represents a leg of the journey on foot.
         */
        record Foot(Stop depStop,
                    LocalDateTime depTime,
                    Stop arrStop,
                    LocalDateTime arrTime)
                implements Leg {
            public Foot {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                // Check that the arrival time is not before the departure time
                Preconditions.checkArgument(!arrTime.isBefore(depTime));

            }
            // Returns an empty list of intermediate stops, as foot legs have none
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }
            // Returns true if the leg is a transfer within the same station
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }
}
