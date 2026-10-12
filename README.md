# ReCHor

An offline journey planner for Swiss public transport, written in Java with a JavaFX interface. Given a departure stop, an arrival stop, a date and a time, it finds every journey that is not beaten on all criteria at once: leaving later, arriving earlier, or changing vehicles fewer times.

Built with Nadine Jegham for EPFL's CS-108 course (*Pratique de la programmation orientée objet*), spring 2025.

<!-- Add a screenshot here: docs/screenshot.png, then uncomment the line below.
![ReCHor showing the journeys from Lausanne to Zermatt](docs/screenshot.png)
-->

## What it does

- **Pareto-optimal journeys.** Instead of a single "best" route, ReCHor lists all journeys that make a different trade-off between departure time, arrival time and number of changes.
- **Fully offline.** Searches run on a local copy of the official Swiss timetable; no network call is needed to plan a trip.
- **Stop search as you type**, tolerant of missing accents and alternative stop names.
- **Journey details** with every leg, platform and walking transfer.
- **Map view** of a journey on [uMap](https://umap.osm.ch), generated as GeoJSON.
- **Calendar export** of a journey as an iCalendar (`.ics`) file.

## How it works

**Timetable storage.** The timetable for the whole country is stored in compact binary files that are memory-mapped rather than loaded into objects. Each record (station, platform, route, trip, connection, transfer) is a fixed-size structure read directly from a `ByteBuffer`, which keeps memory use low and start-up fast.

**Routing.** The router scans every connection of the day once, in decreasing order of departure time, in the style of the Connection Scan Algorithm. For each station and each trip it maintains a Pareto front of the best (arrival time, number of changes) pairs reachable from there. Once the scan is done, the complete set of optimal journeys from any station can be extracted from this profile.

**Packed criteria.** To keep these fronts small and fast to compare, each candidate journey is packed into a single `long`: arrival time, number of changes, departure time and a payload pointing back to the connection that produced it, so that comparing two candidates or checking whether one dominates the other costs only a few bit operations.

**Interface.** The JavaFX interface is built from observable values: changing the stops, the date or the time recomputes the profile and updates the list of journeys and their details automatically.

## Project layout

```text
src/ch/epfl/rechor/
├── timetable/          timetable interfaces (stations, trips, connections…)
│   └── mapped/         memory-mapped binary implementation and caching
├── journey/            router, Pareto fronts, profiles, journey extraction,
│                       GeoJSON and iCalendar conversion
├── gui/                JavaFX interface (query, summary, detail views)
└── …                   stop index, French formatting, JSON and iCal builders
test/                   JUnit 5 tests
resources/              icons and style sheets of the interface
```

## Running it

Requirements: JDK 21 and JavaFX 21.

The timetable data is **not included** in this repository. ReCHor expects it in a folder named `timetable` at the root of the project, in the binary format described above.

1. Open the project in IntelliJ IDEA (or any IDE with JavaFX support).
2. Add JavaFX 21 and JUnit 5 to the project libraries.
3. Place the `timetable` folder at the root of the project.
4. Run `ch.epfl.rechor.gui.Main`.

## Tests

`test/` contains the tests we wrote ourselves:

| Class | What it covers |
|---|---|
| `MyJsonTest` | JSON values and their text form |
| `MyStopIndexTest` | stop search: accents, case, aliases, ranking of results, result limits |
| `journey.MyGeoJsonConverterTest` | GeoJSON conversion of a journey |
| `timetable.mapped.MyCachedTimeTableTest` | caching of the timetable by date, using fake timetables |
| `gui.MySummaryUITest` | a small program, run by hand, that opens the journey summary view |

The JUnit tests run without any external data. The `stopsMatchingWorksWithRealDataFromFileTimeTable` test of `MyStopIndexTest` also checks the stop search on the real timetable when a `timetable` folder is present at the root of the project, and is skipped otherwise. `MySummaryUITest` needs that folder to open its window.

## Credits

The project specification and the overall architecture were designed by the CS-108 teaching team at EPFL. The code in this repository was written by Amine Senhadji and Nadine Jegham. Tests provided by the course were used during development and are not included here.
