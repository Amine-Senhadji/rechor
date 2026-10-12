package ch.epfl.rechor;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MyStopIndexTest {

    Path testPath(String relativePath) {
        String root = System.getenv("RECHOR_TEST_DATA_DIR");
        return root != null ? Path.of(root).resolve(relativePath) : Path.of(relativePath);
    }


    @Test
    void stopsMatchingWorksWithRealDataFromFileTimeTable() throws IOException {
        Path timetablePath = Path.of("timetable");
        TimeTable tt = FileTimeTable.in(timetablePath);

        // Récupère les noms principaux des gares
        List<String> stopNames = IntStream.range(0, tt.stations().size())
                .mapToObj(i -> tt.stations().name(i))
                .toList();

        // Récupère les alias et les mappe aux noms principaux (via Stations)
        Map<String, String> aliases = IntStream.range(0, tt.stationAliases().size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> tt.stationAliases().alias(i),
                        i -> tt.stationAliases().stationName(i)
                ));

        StopIndex index = new StopIndex(stopNames, aliases);

        // Vérifie que "losanna" trouve bien Lausanne
        List<String> result = index.stopsMatching("losanna", 5);
        assertTrue(result.contains("Lausanne"));

        // Vérifie que "mez vil" retourne un des Mézières attendus
        List<String> result2 = index.stopsMatching("mez vil", 5);
        assertTrue(result2.stream().anyMatch(s -> s.toLowerCase().contains("mézières")));
    }


    @Test
    void testStopsMatchingBasic() {
        List<String> stops = List.of("Lausanne", "Renens VD", "Fribourg", "Morges");
        Map<String, String> aliases = Map.of("Losanna", "Lausanne");

        StopIndex index = new StopIndex(stops, aliases);

        List<String> result = index.stopsMatching("laus", 5);
        assertEquals(List.of("Lausanne"), result);

        List<String> result2 = index.stopsMatching("Losanna", 5);
        assertEquals(List.of("Lausanne"), result2);
    }

    @Test
    void testStopsMatchingAccentInsensitive() {
        StopIndex index = new StopIndex(
                List.of("Mézières FR, village", "Charleville-Mézières"),
                Map.of()
        );

        List<String> result = index.stopsMatching("mez vil", 10);
        assertEquals("Mézières FR, village", result.getFirst());
    }

    @Test
    void testStopsMatchingMaxCount() {
        List<String> stops = List.of("A", "B", "C", "D", "E");
        StopIndex index = new StopIndex(stops, Map.of());

        List<String> result = index.stopsMatching("e", 2);
        assertTrue(result.size() <= 2);
    }

    @Test
    void stopsMatchingIsCaseInsensitiveIfQueryHasNoCapital() {
        List<String> stops = List.of("Lausanne", "Renens VD", "Fribourg");
        StopIndex index = new StopIndex(stops, Map.of());

        var result = index.stopsMatching("renens", 3);
        assertEquals(List.of("Renens VD"), result);
    }

    @Test
    void stopsMatchingIsCaseSensitiveIfQueryHasCapital() {
        List<String> stops = List.of("Renens VD", "Renens Sud");
        StopIndex index = new StopIndex(stops, Map.of());

        var result = index.stopsMatching("ReNens", 3);
        assertTrue(result.isEmpty()); // car le Z majuscule désactive l'insensibilité
    }

    @Test
    void stopsMatchingHandlesAliases() {
        List<String> stops = List.of("Lausanne", "Fribourg");
        Map<String, String> aliases = Map.of("Losanna", "Lausanne");

        StopIndex index = new StopIndex(stops, aliases);
        var result = index.stopsMatching("losanna", 3);
        assertEquals(List.of("Lausanne"), result);
    }

    @Test
    void stopsMatchingReturnsEmptyListIfNoMatch() {
        List<String> stops = List.of("Lausanne", "Renens VD");
        StopIndex index = new StopIndex(stops, Map.of());

        var result = index.stopsMatching("nonexistent", 2);
        assertTrue(result.isEmpty());
    }

    @Test
    void stopsMatchingIsSortedByScore() {
        List<String> stops = List.of("Mézières FR, village", "Charleville-Mézières", "Mézery-près-Donneloye, village");
        StopIndex index = new StopIndex(stops, Map.of());

        var result = index.stopsMatching("mez vil", 10);
        assertEquals(List.of("Mézières FR, village", "Mézery-près-Donneloye, village", "Charleville-Mézières"), result);
    }

    @Test
    void stopsMatchingLimitsNumberOfResults() {
        List<String> stops = List.of("A", "B", "C", "D", "E");
        StopIndex index = new StopIndex(stops, Map.of());

        var result = index.stopsMatching("a b c d e", 3);
        assertTrue(result.size() <= 3);
    }

    @Test
    void stopsMatchingIgnoresOrphanAlias() {
        List<String> stops = List.of("Fribourg", "Morges");
        Map<String, String> aliases = Map.of("GhostAlias", "NonExistentStop");

        StopIndex index = new StopIndex(stops, aliases);
        var result = index.stopsMatching("ghost", 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void stopsMatchingWithEmptyOrBlankQueryReturnsEmptyList() {
        StopIndex index = new StopIndex(List.of("Renens", "Lausanne"), Map.of());

        var resultEmpty = index.stopsMatching("", 5);
        var resultSpaces = index.stopsMatching("   ", 5);

        assertTrue(resultEmpty.isEmpty());
        assertTrue(resultSpaces.isEmpty());
    }

    @Test
    void stopsMatchingFavorsExactWordMatch() {
        StopIndex index = new StopIndex(
                List.of("Nyon", "Nyon Sud", "Lausanne"),
                Map.of());

        var result = index.stopsMatching("nyon", 5);
        assertEquals("Nyon", result.getFirst()); // mot exact en début + fin = score max
    }

    @Test
    void stopsMatchingOnlyReturnsCanonicalNameOnceEvenWithMultipleAliases() {
        List<String> stops = List.of("Lausanne");
        Map<String, String> aliases = Map.of(
                "Losanna", "Lausanne",
                "Lôzan", "Lausanne"
        );

        StopIndex index = new StopIndex(stops, aliases);

        var result = index.stopsMatching("lau", 10);
        // Doit contenir une seule fois "Lausanne"
        assertEquals(List.of("Lausanne"), result);
    }


}