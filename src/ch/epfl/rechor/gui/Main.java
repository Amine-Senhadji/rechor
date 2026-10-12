package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.CachedTimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Entry point for the ReCHor application.
 * <p>
 * Loads timetable data, builds a stop index, and sets up the main user interface,
 * including the query panel, journey summary, and detail views. It creates data bindings
 * to compute and display optimal journeys based on user input.
 * </p>
 *
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 * @since 1.0
 */
public class Main extends Application {
    /** Relative path to the timetable data folder. */
    private static final String TIMETABLE_PATH = "timetable";
    /** Title displayed in the application window. */
    private static final String APP_TITLE = "ReCHor";
    /** Minimum width of the application window in pixels. */
    private static final int MIN_WIDTH = 800;
    /** Minimum height of the application window in pixels. */
    private static final int MIN_HEIGHT = 600;
    /** CSS selector used to focus the departure stop text field on startup. */
    private static final String DEP_STOP_FIELD_ID = "#depStop";
    /**
     * Cache mapping a key of the form "date-arrStationId" to a computed {@link Profile},
     * to avoid recomputing journey profiles for the same date and arrival station.
     */
    private final Map<String, Profile> profileMap = new HashMap<>();

    /**
     * Observable holding the current list of journeys computed for the selected query.
     * This prevents garbage collection of the binding and supports updating the UI.
     */
    private ObservableValue<List<Journey>> journeysO;

    /**
     * Entry point of the application.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Application entry point: sets up the UI and binds data to views.
     *
     * @param primaryStage the primary stage for this application
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Load timetable data using CachedTimeTable for improved performance
        TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of(TIMETABLE_PATH)));
        Stations stations = timeTable.stations();
        Router router = new Router(timeTable);

        // 2. Create stop index
        List<String> stationNames = new ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {
            stationNames.add(stations.name(i));
        }

        Map<String, String> stationAliases = new HashMap<>();
        if (timeTable.stationAliases() != null) {
            for (int i = 0; i < timeTable.stationAliases().size(); i++) {
                stationAliases.put(
                        timeTable.stationAliases().alias(i),
                        timeTable.stationAliases().stationName(i)
                );
            }
        }

        StopIndex stopIndex = new StopIndex(stationNames, stationAliases);

        // 3. Create query interface
        QueryUI queryUI = QueryUI.create(stopIndex);

        // 4. Create observable for the list of journeys
        journeysO = Bindings.createObjectBinding(() -> {
            String depStop = queryUI.depStopO().getValue();
            String arrStop = queryUI.arrStopO().getValue();
            LocalDate date = queryUI.dateO().getValue();

            // Validate that required parameters are provided
            if (depStop == null || arrStop == null || depStop.isEmpty() ||
                    arrStop.isEmpty() || date == null) {
                return List.of();
            }

            int depStationId = stationId(stations, depStop);
            int arrStationId = stationId(stations, arrStop);

            if (depStationId == -1 || arrStationId == -1) {
                return List.of();
            }

            String cacheKey = date + "-" + arrStationId;
            final int finalArrStationId = arrStationId;
            Profile profile = profileMap.computeIfAbsent(cacheKey, k -> router.profile(date, finalArrStationId));

            return JourneyExtractor.journeys(profile, depStationId);
        }, queryUI.depStopO(), queryUI.arrStopO(), queryUI.dateO());

        SummaryUI summaryUI = SummaryUI.create(journeysO, queryUI.timeO());
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO());

        BorderPane root = new BorderPane();
        root.setTop(queryUI.rootNode());

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(summaryUI.rootNode(), detailUI.rootNode());
        splitPane.setDividerPositions(0.5);

        summaryUI.rootNode().minWidth(350);
        detailUI.rootNode().minWidth(350);

        root.setCenter(splitPane);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.show();

        Platform.runLater(() -> scene.lookup(DEP_STOP_FIELD_ID).requestFocus());
    }
    /**
     * Returns the ID of a station with the given name, or -1 if the name does not match any station.
     *
     * @param stations the list of stations
     * @param name the name of the station to find
     * @return the station ID, or -1 if not found
     */
    private int stationId(Stations stations, String name) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }
}