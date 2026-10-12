package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;

import ch.epfl.rechor.timetable.*;
import ch.epfl.rechor.timetable.mapped.CachedTimeTable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Objects;

class MyCachedTimeTableTest {

    // ---------------------------------------------------------------
    // Fake implementations used for testing purposes
    // ---------------------------------------------------------------

    private static class FakeTrips implements Trips {
        private final LocalDate date;
        FakeTrips(LocalDate date) {
            this.date = date;
        }
        @Override
        public String toString() {
            return "FakeTrips:" + date;
        }

        @Override
        public int routeId(int id) {
            return 0;
        }

        @Override
        public String destination(int id) {
            return "";
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private static class FakeConnections implements Connections {
        private final LocalDate date;
        FakeConnections(LocalDate date) {
            this.date = date;
        }
        @Override
        public String toString() {
            return "FakeConnections:" + date;
        }

        @Override
        public int depStopId(int id) {
            return 0;
        }

        @Override
        public int depMins(int id) {
            return 0;
        }

        @Override
        public int arrStopId(int id) {
            return 0;
        }

        @Override
        public int arrMins(int id) {
            return 0;
        }

        @Override
        public int tripId(int id) {
            return 0;
        }

        @Override
        public int tripPos(int id) {
            return 0;
        }

        @Override
        public int nextConnectionId(int id) {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private static class FakeStations implements Stations {
        private final String name;
        FakeStations(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return "Stations:" + name;
        }

        @Override
        public String name(int id) {
            return "";
        }

        @Override
        public double longitude(int id) {
            return 0;
        }

        @Override
        public double latitude(int id) {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private static class FakeTimeTable implements TimeTable {
        int tripsCalls = 0;
        int connectionsCalls = 0;
        private final Stations stations;

        FakeTimeTable(Stations stations) {
            this.stations = stations;
        }

        @Override
        public Trips tripsFor(LocalDate date) {
            // Ensure date is not null.
            Objects.requireNonNull(date, "Date must not be null");
            tripsCalls++;
            return new FakeTrips(date);
        }

        @Override
        public Connections connectionsFor(LocalDate date) {
            Objects.requireNonNull(date, "Date must not be null");
            connectionsCalls++;
            return new FakeConnections(date);
        }

        @Override
        public Stations stations() {
            return stations;
        }

        @Override
        public StationAliases stationAliases() {
            return null;
        }

        @Override
        public Platforms platforms() {
            return null;
        }

        @Override
        public Routes routes() {
            return null;
        }

        @Override
        public Transfers transfers() {
            return null;
        }

    }

    // ---------------------------------------------------------------
    // Test cases
    // ---------------------------------------------------------------

    /**
     * Tests that for the same date, tripsFor returns a cached instance and
     * the underlying timetable's tripsFor is called only once.
     */
    @Test
    void testTripsCachingSameDate() {
        FakeStations fakeStations = new FakeStations("FakeStation");
        FakeTimeTable fakeTT = new FakeTimeTable(fakeStations);
        CachedTimeTable cacheTT = new CachedTimeTable(fakeTT);

        LocalDate date = LocalDate.of(2023, 1, 1);
        Trips trips1 = cacheTT.tripsFor(date);
        Trips trips2 = cacheTT.tripsFor(date);

        assertSame(trips1, trips2,
                "Trips for the same date must be cached (same instance).");
        assertEquals(1, fakeTT.tripsCalls,
                "Underlying timetable tripsFor should be called once for the same date.");
    }

    /**
     * Tests that different dates yield different Trips instances and that the underlying timetable
     * is queried for each new date.
     */
    @Test
    void testTripsCachingDifferentDates() {
        FakeStations fakeStations = new FakeStations("FakeStation");
        FakeTimeTable fakeTT = new FakeTimeTable(fakeStations);
        CachedTimeTable cacheTT = new CachedTimeTable(fakeTT);

        LocalDate date1 = LocalDate.of(2023, 1, 1);
        LocalDate date2 = LocalDate.of(2023, 1, 2);
        Trips trips1 = cacheTT.tripsFor(date1);
        Trips trips2 = cacheTT.tripsFor(date2);

        assertNotSame(trips1, trips2,
                "Trips for different dates must not be the same instance.");
        assertEquals(2, fakeTT.tripsCalls,
                "Underlying timetable tripsFor should be called once per new date.");
    }

    /**
     * Tests that for the same date, connectionsFor returns a cached instance and that the underlying
     * timetable's connectionsFor is called only once.
     */
    @Test
    void testConnectionsCachingSameDate() {
        FakeStations fakeStations = new FakeStations("FakeStation");
        FakeTimeTable fakeTT = new FakeTimeTable(fakeStations);
        CachedTimeTable cacheTT = new CachedTimeTable(fakeTT);

        LocalDate date = LocalDate.of(2023, 1, 1);
        Connections conns1 = cacheTT.connectionsFor(date);
        Connections conns2 = cacheTT.connectionsFor(date);

        assertSame(conns1, conns2,
                "Connections for the same date must be cached (same instance).");
        assertEquals(1, fakeTT.connectionsCalls,
                "Underlying timetable connectionsFor should be called once for the same date.");
    }

    /**
     * Tests that calling connectionsFor with different dates yields different instances and that the cache is
     * updated accordingly.
     */
    @Test
    void testConnectionsCachingDifferentDates() {
        FakeStations fakeStations = new FakeStations("FakeStation");
        FakeTimeTable fakeTT = new FakeTimeTable(fakeStations);
        CachedTimeTable cacheTT = new CachedTimeTable(fakeTT);

        LocalDate date1 = LocalDate.of(2023, 1, 1);
        LocalDate date2 = LocalDate.of(2023, 1, 2);
        Connections conns1 = cacheTT.connectionsFor(date1);
        Connections conns2 = cacheTT.connectionsFor(date2);

        assertNotSame(conns1, conns2,
                "Connections for different dates must not be the same instance.");
        assertEquals(2, fakeTT.connectionsCalls,
                "Underlying timetable connectionsFor should be called once per new date.");
    }

    /**
     * Tests that methods other than tripsFor and connectionsFor simply delegate to the underlying timetable.
     */
    @Test
    void testDelegation() {
        FakeStations fakeStations = new FakeStations("FakeStation");
        FakeTimeTable fakeTT = new FakeTimeTable(fakeStations);
        CachedTimeTable cacheTT = new CachedTimeTable(fakeTT);

        // Assuming the TimeTable interface has the method stations() for delegation:
        assertSame(fakeStations, cacheTT.stations(),
                "Methods not managed by the cache (like stations()) should delegate to the underlying timetable.");
    }

    /**
     * Tests that passing a null date to tripsFor causes a NullPointerException.
     */
    @Test
    void testNullTripsDate() {
        FakeStations fakeStations = new FakeStations("FakeStation");
        FakeTimeTable fakeTT = new FakeTimeTable(fakeStations);
        CachedTimeTable cacheTT = new CachedTimeTable(fakeTT);

        assertThrows(NullPointerException.class, () -> {
            cacheTT.tripsFor(null);
        }, "Passing a null date to tripsFor should throw a NullPointerException.");
    }

    /**
     * Tests that passing a null date to connectionsFor causes a NullPointerException.
     */
    @Test
    void testNullConnectionsDate() {
        FakeStations fakeStations = new FakeStations("FakeStation");
        FakeTimeTable fakeTT = new FakeTimeTable(fakeStations);
        CachedTimeTable cacheTT = new CachedTimeTable(fakeTT);

        assertThrows(NullPointerException.class, () -> {
            cacheTT.connectionsFor(null);
        }, "Passing a null date to connectionsFor should throw a NullPointerException.");
    }
}
