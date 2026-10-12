package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a journey profile that encapsulates the timetable, travel date, arrival station identifier,
 * and the Pareto frontier for each station.
 *
 * @param timeTable the timetable for the journey
 * @param date the travel date
 * @param arrStationId the arrival station identifier
 * @param stationFront the list of Pareto fronts for each station
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public record Profile(
        TimeTable timeTable,
        LocalDate date,
        int arrStationId,
        List<ParetoFront> stationFront
)  {


    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    /**
     * Returns the connections for the journey on the given date.
     *
     * @return the connections for the journey
     */
    public Connections connections() {
        return timeTable.connectionsFor(date);
    }

    /**
     * Returns the trips for the journey on the given date.
     *
     * @return the trips for the journey
     */
    public Trips trips() {
        return timeTable.tripsFor(date);
    }

    /**
     * Returns the Pareto front for the given station id.
     *
     * @param stationId the station id
     * @return the Pareto front for the station
     * @throws IndexOutOfBoundsException if the station id is invalid
     */
    public ParetoFront forStation(int stationId) {
        if (stationId < 0 || stationId >= stationFront.size()) {
            throw new IndexOutOfBoundsException();
        }
        return stationFront.get(stationId);
    }

    public static final class Builder {
        private final TimeTable timeTable;
        private final LocalDate date;
        private final int arrStationId;
        // Array to store builders for each station.
        private final ParetoFront.Builder[] stationBuilders;
        // Array to store builders for each trip (if needed).
        private final ParetoFront.Builder[] tripBuilders;

        /**
         * Constructs a Builder for a journey profile.
         *
         * @param timeTable the timetable for the journey
         * @param date the travel date
         * @param arrStationId the arrival station id
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            Preconditions.checkArgument(timeTable != null);
            Preconditions.checkArgument(date != null);
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            // Initialiser le tableau pour les gares avec la taille du nombre de gares
            int numStations = timeTable.stations().size();
            stationBuilders = new ParetoFront.Builder[numStations];
            // Initialiser le tableau pour les courses avec la taille du nombre de courses
            int numTrips = timeTable.tripsFor(date).size();
            tripBuilders = new ParetoFront.Builder[numTrips];
        }

        /**
         * Returns the Pareto front builder for the given station.
         *
         * @param stationId the station id
         * @return the builder for the station's Pareto front
         * @throws IndexOutOfBoundsException if the station id is invalid
         */
        public ParetoFront.Builder forStation(int stationId) {
            if (stationId < 0 || stationId >= stationBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            return stationBuilders[stationId];
        }

        /**
         * Sets the Pareto front builder for the given station.
         *
         * @param stationId the station id
         * @param builder the Pareto front builder for the station
         * @throws IndexOutOfBoundsException if the station id is invalid
         */
        public void setForStation(int stationId, ParetoFront.Builder builder) {
            if (stationId < 0 || stationId >= stationBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            stationBuilders[stationId] = builder;
        }

        /**
         * Returns the Pareto front builder for the given trip.
         *
         * @param tripId the trip id
         * @return the builder for the trip's Pareto front
         * @throws IndexOutOfBoundsException if the trip id is invalid
         */
        public ParetoFront.Builder forTrip(int tripId) {
            if (tripId < 0 || tripId >= tripBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            return tripBuilders[tripId];
        }

        /**
         * Sets the Pareto front builder for the given trip.
         *
         * @param tripId the trip id
         * @param builder the Pareto front builder for the trip
         * @throws IndexOutOfBoundsException if the trip id is invalid
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            if (tripId < 0 || tripId >= tripBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            tripBuilders[tripId] = builder;
        }

        /**
         * Builds and returns the Profile instance from the collected builders.
         *
         * @return the constructed Profile
         */
        public Profile build() {
            ParetoFront[] stationFronts = new ParetoFront[stationBuilders.length];
            for (int i = 0; i < stationBuilders.length; i++) {
                stationFronts[i] = (stationBuilders[i] == null)
                        ? ParetoFront.EMPTY
                        : stationBuilders[i].build();
            }

            return new Profile(timeTable, date, arrStationId, List.of(stationFronts));
        }
    }
}
