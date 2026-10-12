package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class responsible for extracting optimal {@link Journey} instances from a {@link Profile}.
 * <p>
 * This class provides the algorithmic logic to reconstruct complete journeys from Pareto-optimal
 * criteria stored in the profile. It traverses the set of best journeys from a specified departure
 * station to the target arrival station, building the sequence of {@link Journey.Leg} steps, including
 * foot and transport legs.
 * </p>
 * <p>
 * The class is not instantiable and is designed solely for use through its static methods.
 * </p>
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class JourneyExtractor {

    private JourneyExtractor() {}

    /**
     * Extracts all optimal journeys starting from a given departure station.
     * Iterates over the Pareto-optimal criteria for the specified station,
     * constructs each journey's legs, and returns the list of journeys
     * sorted by departure time and then by arrival time
     *
     * @param profile      the journey search profile containing connections, timetable, etc.
     * @param depStationId the ID of the departure station
     * @return sorted list of Journey instances by departure then arrival time
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        TimeTable timeTable = profile.timeTable();
        int arrStationId = profile.arrStationId();
        int nextStationId = timeTable.stationId(depStationId);

        // Special case: if departure equals arrival, return an empty list.
        if (nextStationId == arrStationId) {
            return journeys;
        }

        ParetoFront front = profile.forStation(nextStationId);
        front.forEach(criteria -> {
            List<Journey.Leg> legs = new ArrayList<>();
            makeJourney(criteria, nextStationId, profile, legs, arrStationId);
            if (!legs.isEmpty()) {
                journeys.add(new Journey(legs));
            }
        });

        journeys.sort(Comparator.comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));
        return journeys;
    }

    /**
     * Recursively constructs a journey from the given criteria.
     * Builds the legs of a journey by iterating over connections and adding
     * transport or foot legs as needed.
     *
     * @param criteria     the packed criteria for the journey
     * @param depStationId the ID of the departure station
     * @param profile      the journey search profile
     * @param legs         the list of journey legs being constructed
     * @param arrStationId the ID of the target arrival station
     */
    private static void makeJourney(long criteria,
                                    int depStationId,
                                    Profile profile,
                                    List<Journey.Leg> legs,
                                    int arrStationId) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();


        int depMins = PackedCriteria.depMins(criteria);
        int arrMins = PackedCriteria.arrMins(criteria);
        int changes = PackedCriteria.changes(criteria);
        int connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(criteria));
        int intermediateStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
        int connDepStopId = connections.depStopId(connectionId);
        int connDepStationId = timeTable.stationId(connDepStopId);

        // Add initial foot leg if departure stations differ
        if (depStationId != connDepStationId) {
            addingFootLeg(profile, depStationId, connDepStopId, depMins, legs);
        }

        // Add the first transport leg
        connectionId = addingTransportLeg(profile, connectionId, legs, intermediateStops);
        int arrStopId = connections.arrStopId(connectionId);
        int arrStationId1 = timeTable.stationId(arrStopId);

        // If destination is reached or no changes remain
        if (arrStationId1 == arrStationId || changes == 0) {
            if (arrStationId1 != arrStationId) {
                addingFootLeg(profile,
                        arrStopId,
                        arrStationId,
                        connections.arrMins(connectionId),
                        legs);
            }
            return;
        }

        // Find next criteria for the continuation of the journey
        long nextCriteria = detectNextCriteria(profile,
                arrStationId1,
                arrMins,
                changes - 1);
        if (nextCriteria == -1) {
            return; // No matching criteria found
        }

        // Extract next connection info
        int nextConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextCriteria));
        int nextDepStopId = connections.depStopId(nextConnectionId);

        // Add transfer foot leg
        addingFootLeg(profile,
                arrStopId,
                nextDepStopId,
                connections.arrMins(connectionId),
                legs);

        // Continue recursively
        makeJourney(nextCriteria,
                timeTable.stationId(nextDepStopId),
                profile,
                legs,
                arrStationId);
    }

    /**
     * Adds a transport leg to the journey.
     *
     * @param profile             the journey search profile
     * @param connectionId        the ID of the connection
     * @param legs                the list of journey legs being constructed
     * @param intermediateStops   the number of intermediate stops
     * @return the updated connection ID after adding the transport leg
     */
    private static int addingTransportLeg(Profile profile,
                                          int connectionId,
                                          List<Journey.Leg> legs,
                                          int intermediateStops) {
        TimeTable timeTable = profile.timeTable();
        Stations stations = timeTable.stations();
        Connections connections = profile.connections();
        Trips trips = profile.trips();
        Routes routes = timeTable.routes();
        LocalDate date = profile.date();

        int tripId = connections.tripId(connectionId);
        int[] result = parseStops(connections, connectionId, tripId, intermediateStops);
        int lastConnectionId = result[0];
        int stopsParsed = result[1];

        int depStopId = connections.depStopId(connectionId);
        int arrStopId = connections.arrStopId(lastConnectionId);
        int depMins = connections.depMins(connectionId);
        int arrMins = connections.arrMins(lastConnectionId);

        Stop depStop = legs.isEmpty() || !(legs.getLast() instanceof Journey.Leg.Foot)
                ? createStop(stations, timeTable, depStopId)
                : legs.getLast().arrStop();
        Stop arrStop = createStop(stations, timeTable, arrStopId);

        LocalDateTime depTime = convertToDateTime(date, depMins);
        LocalDateTime arrTime = convertToDateTime(date, arrMins);

        // Collect intermediate stops
        List<Journey.Leg.IntermediateStop> intermediateStopList =
                storesIntermediateStops(timeTable,
                        connections,
                        connectionId,
                        stopsParsed,
                        date);

        // Advance connection ID to the last connection
        for (int i = 0; i < stopsParsed; i++) {
            connectionId = connections.nextConnectionId(connectionId);
        }

        int routeId = trips.routeId(tripId);
        legs.add(new Journey.Leg.Transport(
                depStop,
                depTime,
                arrStop,
                arrTime,
                intermediateStopList,
                routes.vehicle(routeId),
                routes.name(routeId),
                trips.destination(tripId)));

        return connectionId;
    }

    /**
     * Adds a foot leg to the journey.
     *
     * @param profile       the journey search profile
     * @param depStopId     the ID of the departure stop
     * @param arrStopId     the ID of the arrival stop
     * @param depMins       the departure time in minutes
     * @param legs          the list of journey legs being constructed
     */
    private static void addingFootLeg(Profile profile,
                                      int depStopId,
                                      int arrStopId,
                                      int depMins,
                                   List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Stations stations = timeTable.stations();
        Transfers transfers = timeTable.transfers();
        LocalDate date = profile.date();

        int fromStation = timeTable.stationId(depStopId);
        int toStation = timeTable.stationId(arrStopId);

        Stop depStop = legs.isEmpty() ? createStop(stations, timeTable, depStopId)
                : legs.getLast().arrStop();
        Stop arrStop = createStop(stations, timeTable, arrStopId);

        int walkMinutes = transfers.minutesBetween(fromStation, toStation);
        LocalDateTime depTime = convertToDateTime(date, depMins);
        LocalDateTime arrTime = convertToDateTime(date, depMins + walkMinutes);

        legs.add(new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime));
    }

    /**
     * Traverses intermediate stops for a transport leg.
     *
     * @param connections        connection data
     * @param startConnectionId  starting connection ID
     * @param tripId             trip identifier
     * @param intermediateStops  number of intermediate stops to traverse
     * @return an array where index 0 is the last connection ID and index 1 is stops traversed
     */
    private static int[] parseStops(Connections connections,
                                    int startConnectionId,
                                    int tripId,
                                    int intermediateStops) {
        int lastConnectionId = startConnectionId;
        int stopsParsed = 0;
        while (stopsParsed < intermediateStops) {
            int nextConnId = connections.nextConnectionId(lastConnectionId);
            if (nextConnId < 0 || connections.tripId(nextConnId) != tripId) {
                break;
            }
            lastConnectionId = nextConnId;
            stopsParsed++;
        }
        return new int[] { lastConnectionId, stopsParsed };
    }

    /**
     * Collects intermediate stops between connections.
     *
     * @param timeTable          the timetable data
     * @param connections        connection data
     * @param startConnectionId  starting connection ID
     * @param stopsCount         number of stops to collect
     * @param date               the journey date
     * @return list of intermediate stops
     */
    private static List<Journey.Leg.IntermediateStop> storesIntermediateStops(
            TimeTable timeTable, Connections connections, int startConnectionId,
            int stopsCount, LocalDate date) {
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
        if (stopsCount > 0) {
            int currConnId = startConnectionId;
            for (int i = 0; i < stopsCount; i++) {

                int nextConnId = connections.nextConnectionId(currConnId);
                int intermediateStopId = connections.arrStopId(currConnId);

                int intermArrMins = connections.arrMins(currConnId);
                int intermDepMins = connections.depMins(nextConnId);

                LocalDateTime intermArrDateTime = convertToDateTime(date, intermArrMins);
                LocalDateTime intermDepDateTime = convertToDateTime(date, intermDepMins);

                Stop intermediateStop = createStop(
                        timeTable.stations(),
                        timeTable,
                        intermediateStopId);

                intermediateStops.add(
                        new Journey.Leg.IntermediateStop(
                        intermediateStop,
                                intermArrDateTime,
                                intermDepDateTime));
                currConnId = nextConnId;
            }
        }
        return intermediateStops;
    }

    /**
     *
     * Finds the matching criteria from the Pareto front.
     *
     * @param profile       the profile with connection data
     * @param stationId     the station ID to lookup
     * @param targetArrMins target arrival time in minutes
     * @param targetChanges target number of changes
     * @return matching criteria or -1 if none found
     */
    private static long detectNextCriteria(Profile profile,
                                           int stationId,
                                           int targetArrMins,
                                           int targetChanges) {
        ParetoFront front = profile.forStation(stationId);
        long[] sameCriteria = { -1 };
        front.forEach(criteria -> {
            if (PackedCriteria.arrMins(criteria) <= targetArrMins &&
                    PackedCriteria.changes(criteria) == targetChanges) {
                if (sameCriteria[0] == -1 ||
                        PackedCriteria.arrMins(criteria) > PackedCriteria.arrMins(sameCriteria[0])){
                    sameCriteria[0] = criteria;
                }
            }
        });
        return sameCriteria[0];
    }

    /**
     * Creates a Stop instance for the given stop ID.
     *
     * @param stations   the list of stations
     * @param timeTable  the timetable containing stop information
     * @param stopId     the ID of the stop
     * @return the created Stop instance
     */
    private static Stop createStop(Stations stations, TimeTable timeTable, int stopId) {
        int stationId = timeTable.stationId(stopId);
        return new Stop(
                stations.name(stationId),
                timeTable.platformName(stopId),
                stations.longitude(stationId),
                stations.latitude(stationId));
    }

    /**
     * Converts a time in minutes to a LocalDateTime.
     *
     * @param date    the date of the journey
     * @param minutes the time in minutes since midnight
     * @return the corresponding LocalDateTime
     */
    private static LocalDateTime convertToDateTime(LocalDate date, int minutes) {
         return date.atStartOfDay().plusMinutes(minutes);

    }
}