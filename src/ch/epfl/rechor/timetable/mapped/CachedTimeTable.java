package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A TimeTable implementation that caches the date-dependent data (Trips and Connections)
 * to avoid reloading or memory-mapping the same day's data multiple times.
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public class CachedTimeTable implements TimeTable {
    private final TimeTable underlying;

    // Cached connections and the date they correspond to
    private Connections cachedConnections;
    private LocalDate connectionsDate;

    // Cached trips and the date they correspond to
    private Trips cachedTrips;
    private LocalDate tripsDate;

    /**
     * Constructs a CachedTimeTable wrapping the given underlying timetable.
     * @param underlying the TimeTable whose date-dependent data should be cached
     */
    public CachedTimeTable(TimeTable underlying) {
        this.underlying = Objects.requireNonNull(underlying);
    }

    @Override
    public Stations stations() {
        return underlying.stations();
    }

    @Override
    public StationAliases stationAliases() {
        return underlying.stationAliases();
    }

    @Override
    public Platforms platforms() {
        return underlying.platforms();
    }

    @Override
    public Routes routes() {
        return underlying.routes();
    }

    @Override
    public Transfers transfers() {
        return underlying.transfers();
    }

    /**
     * Returns the connections for the given date, using the cache if possible.
     *
     * @param date the date for which to retrieve connections
     * @return a Connections instance for the date, cached if previously loaded
     */
    @Override
    public Connections connectionsFor(LocalDate date) {

        if (connectionsDate == null || !connectionsDate.equals(date)) {
            cachedConnections = underlying.connectionsFor(date);
            connectionsDate = date;
        }

        return cachedConnections;
    }

    /**
     * Returns the trips for the given date, using the cache if possible.
     *
     * @param date the date for which to retrieve trips
     * @return a Trips instance for the date, cached if previously loaded
     */
    @Override
    public Trips tripsFor(LocalDate date) {

        if (tripsDate == null || !tripsDate.equals(date)) {
            cachedTrips = underlying.tripsFor(date);
            tripsDate = date;
        }

        return cachedTrips;
    }

    // The default methods (isStationId, isPlatformId, stationId, platformName)
    // are inherited from the interface and automatically delegate appropriately.
}
