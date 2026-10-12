package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.CachedTimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.NoSuchElementException;

public final class MySummaryUITest extends Application {
    public static void main(String[] args) { launch(args); }

    static int stationId(Stations stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Station not found: " + stationName);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TimeTable timeTable = new CachedTimeTable(
                FileTimeTable.in(Path.of("timetable")));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025, Month.APRIL, 28);
        int depStationId = stationId(stations, "Ecublens VD, EPFL");
        int arrStationId = stationId(stations, "Gruyères");
        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);
        List<Journey> journeys = JourneyExtractor
                .journeys(profile, depStationId);

        ObservableValue<List<Journey>> journeysO =
                new SimpleObjectProperty<>(journeys);
        ObservableValue<LocalTime> depTimeO =
                new SimpleObjectProperty<>(LocalTime.of(16, 0));
        SummaryUI summaryUI = SummaryUI.create(journeysO, depTimeO);
        Pane root = new BorderPane(summaryUI.rootNode());
        Scene scene = new Scene(root, 600, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Vue d'ensemble des voyages");
        primaryStage.show();
    }
}