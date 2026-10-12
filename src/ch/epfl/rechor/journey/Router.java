package ch.epfl.rechor.journey;
import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.*;

import java.time.LocalDate;
import java.util.Arrays;
/**
 * Router responsible for computing journey search profiles.
 * <p>
 * Builds Pareto-optimal criteria for a given arrival station and date
 * by incorporating walking transfers and trip continuation logic.
 * </p>
 *
 * @param timeTable timetable providing station, transfer, and connection data
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 * @version 1.0
 * @since 1.0
 */
public record Router(TimeTable timeTable) {
    /** Marker for no walking transfer available. */
    private static final int NO_WALK = -1;
    /** Initial count for zero changes in criterion. */
    private static final int NO_CHANGE = 0;
    /**
     * Builds a journey search profile for journeys ending at the specified station on a date.
     * Computes walking transfer times, iterates over all connections,
     * and constructs Pareto fronts for stations and trips.
     *
     * @param date the travel date
     * @param arrStationId the ID of the arrival station
     * @return a Profile containing optimal journey criteria
     */
    public Profile profile(LocalDate date, int arrStationId) {
        int length = timeTable.stations().size();

        int[] walkingTimes = new int[length];

        Arrays.fill(walkingTimes, NO_WALK);

        Transfers transfers = timeTable.transfers();

        int interval= transfers.arrivingAt(arrStationId);
        int start = PackedRange.startInclusive(interval);
        int end = PackedRange.endExclusive(interval);

        for (int i = start; i < end; i++) {
            walkingTimes[transfers.depStationId(i)] = transfers.minutes(i);
        }

        Profile.Builder profileBuilder = new Profile.Builder(timeTable, date, arrStationId);
        Connections connections = timeTable.connectionsFor(date);

        for (int connId = 0; connId < connections.size(); connId++) {

            int arrStopId = timeTable.stationId(connections.arrStopId(connId));
            int depStationId = timeTable.stationId(connections.depStopId(connId));
            int arrMin = connections.arrMins(connId);
            int tripId = connections.tripId(connId);

            ParetoFront.Builder paretoFront = new ParetoFront.Builder();

            // Criterion 1: direct walking transfer to arrival station
            int walkTime = walkingTimes[arrStopId];
            if (walkTime != NO_WALK) {
                paretoFront.add(
                        arrMin + walkTime,
                        NO_CHANGE,
                        Bits32_24_8.pack(connId, 0));
            }

            // Criterion 2: continuation of existing trip
            ParetoFront.Builder tripFront = profileBuilder.forTrip(tripId);
            if (tripFront != null) {
                paretoFront.addAll(tripFront);
            }

            // Criterion 3: arrival at intermediate stop with possible change
            ParetoFront.Builder arrivalFront = profileBuilder.forStation(arrStopId);
            if (arrivalFront != null) {
                final int finalConnId = connId; // pour lambda
                final int connArrMin = arrMin;

                arrivalFront.forEach(criteria -> {
                    if (PackedCriteria.depMins(criteria) >= connArrMin) {
                        long updatedCriteria = PackedCriteria.withoutDepMins(
                                PackedCriteria.withPayload(
                                        PackedCriteria.withAdditionalChange(criteria),
                                        Bits32_24_8.pack(finalConnId, 0)
                                )
                        );
                        paretoFront.add(updatedCriteria);
                    }
                });
            }
            if (paretoFront.isEmpty()) continue;
            // Set or update trip Pareto front
            if (profileBuilder.forTrip(tripId) == null) {
                profileBuilder.setForTrip(tripId, paretoFront);

            } else {
                profileBuilder.forTrip(tripId).addAll(paretoFront);
            }


            // Criterion 4: updating profiles with possible walking transfers at departure
            int depMin = connections.depMins(connId);
            int interval1 = transfers.arrivingAt(depStationId);
            int startIndex = PackedRange.startInclusive(interval1);
            int endIndex = PackedRange.endExclusive(interval1);

            for (int i = startIndex; i < endIndex; i++) {
                int dep = depMin - transfers.minutes(i);
                int fromStation = transfers.depStationId(i);
                ParetoFront.Builder withDepBuilder = new ParetoFront.Builder();
                // For each criterion, add a new tuple with updated departure time
                int finalConnId = connId;
                paretoFront.forEach(criteria -> {
                    int payloadConnId = Bits32_24_8.unpack24(PackedCriteria.payload(criteria));
                    int intermediatesStop = connections.tripPos(payloadConnId)
                            - connections.tripPos(finalConnId);
                    int payload = Bits32_24_8.pack(finalConnId, intermediatesStop);

                    long updatesTuple = PackedCriteria.withDepMins(
                            PackedCriteria.withPayload(criteria, payload), dep);

                    withDepBuilder.add(updatesTuple);
                });
                ParetoFront.Builder stationFront = profileBuilder.forStation(fromStation);
                if (stationFront == null) {
                    profileBuilder.setForStation(fromStation, withDepBuilder);
                } else {
                    stationFront.addAll(withDepBuilder);
                }
            }
        }
        return profileBuilder.build();
    }
}