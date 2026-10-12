package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * Graphical component displaying detailed information about the selected journey.
 * <p>
 * Provides a scrollable view that shows either a placeholder when no journey is selected
 * or a detailed grid of journey steps (foot and transport legs), together with
 * "Map" and "Calendar" buttons for exporting the journey.
 * </p>
 *
 * @param rootNode the root JavaFX node of the detail view (a ScrollPane)
 */
public record DetailUI(Node rootNode) {
    /** Radius in pixels of circles marking departure and arrival stops. */
    private static final int CIRCLE_RADIUS = 3;
    /** Size in pixels for the transport leg icon images. */
    private static final int ICON_SIZE = 31;
    /** Horizontal spacing in pixels between export buttons. */
    private static final int BUTTON_SPACING = 10;
    /** Vertical spacing in pixels between elements within the journey pane. */
    private static final int JOURNEY_PANE_SPACING = 10;
    /** Padding in pixels around the content of the journey pane. */
    private static final int JOURNEY_PANE_PADDING = 10;
    /**
     * Creates a DetailUI that observes the given journey observable.
     * When the selected journey changes, the view updates to show the journey steps
     * or a placeholder if null.
     *
     * @param journeyObservable observable value holding the current Journey or null
     * @return a new DetailUI instance with dynamic content
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservable) {
        // Create and configure the root ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        String cssPath = Objects.requireNonNull(DetailUI.class.getResource("/detail.css")).toExternalForm();
        scrollPane.getStylesheets().add(cssPath);
        scrollPane.setId("detail");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


        StackPane noJourneyPane = new StackPane(new Label("Aucun voyage"));
        noJourneyPane.setId("no-journey");


        VBox journeyPane = new VBox();
        journeyPane.setId("journey");
        journeyPane.setSpacing(JOURNEY_PANE_SPACING);
        journeyPane.setPadding(new Insets(JOURNEY_PANE_PADDING));


        StackPane stepsContainer = new StackPane();
        Pane annotationsPane = new Pane();
        annotationsPane.setPickOnBounds(false);


        HBox buttonsPane = new HBox(BUTTON_SPACING);
        buttonsPane.setAlignment(Pos.CENTER);
        buttonsPane.setId("buttons");
        Button mapButton = new Button("Carte");
        Button calendarButton = new Button("Calendrier");
        buttonsPane.getChildren().addAll(mapButton, calendarButton);


        journeyPane.getChildren().addAll(stepsContainer, buttonsPane);

        configureMapButton(mapButton, journeyObservable);
        configureCalendarButton(calendarButton, journeyObservable);

        StackPane contentPane = new StackPane(noJourneyPane, journeyPane);


        journeyObservable.subscribe(newJourney -> {
            annotationsPane.getChildren().clear();
            if (newJourney != null) {
                StepsGridPane stepsGrid = new StepsGridPane(newJourney, annotationsPane);
                stepsContainer.getChildren().clear();
                stepsContainer.getChildren().addAll(annotationsPane, stepsGrid);
                noJourneyPane.setVisible(false);
                journeyPane.setVisible(true);
            } else {
                stepsContainer.getChildren().clear();
                noJourneyPane.setVisible(true);
                journeyPane.setVisible(false);
            }
        });

        scrollPane.setContent(contentPane);
        return new DetailUI(scrollPane);
    }

    /**
     * Grid pane displaying each leg of the journey with connecting annotations.
     * Iterates through journey legs, adding a text row for foot legs and detailed
     * rows (times, stations, intermediate stops) for transport legs.
     */
    private static class StepsGridPane extends GridPane {
        private final List<Circle[]> circles = new ArrayList<>();
        private final Pane annotations;

        /**
         * Constructs a grid pane for displaying journey legs with annotation support.
         *
         * @param journey the journey whose legs will be displayed
         * @param annotations pane for drawing connecting lines between legs
         */
        private StepsGridPane(Journey journey, Pane annotations) {
            this.setId("legs");
            this.annotations = annotations;

            List<Journey.Leg> legs = journey.legs();
            int row = 0;

            for (Journey.Leg leg : legs) {
                if (leg instanceof Journey.Leg.Foot footLeg) {
                    Text footText = new Text(FormatterFr.formatLeg(footLeg));
                    GridPane.setColumnSpan(footText, 2);
                    this.add(footText, 2, row);
                    row++;
                } else if (leg instanceof Journey.Leg.Transport transport) {
                    Text depTimeText = new Text(FormatterFr.formatTime(transport.depTime()));
                    depTimeText.getStyleClass().add("departure");
                    this.add(depTimeText, 0, row);
                    GridPane.setHalignment(depTimeText, HPos.RIGHT);

                    Circle depCircle = new Circle(CIRCLE_RADIUS, Color.BLACK);
                    this.add(depCircle, 1, row);

                    Text depStationText = new Text(transport.depStop().name());
                    this.add(depStationText, 2, row);

                    Text platformText = new Text(transport.depStop().platformName() != null ?
                            FormatterFr.formatPlatformName(transport.depStop()) : "");
                    platformText.getStyleClass().add("departure");
                    this.add(platformText, 3, row);

                    ImageView iconImage = new ImageView(VehicleIcons.iconFor(transport.vehicle()));
                    iconImage.setFitHeight(ICON_SIZE);
                    iconImage.setFitWidth(ICON_SIZE);

                    Text routeDestText = new Text(FormatterFr.formatRouteDestination(transport));
                    GridPane.setColumnSpan(routeDestText, 2);
                    this.add(routeDestText, 2, row + 1);

                    int intermediateStopsCount = transport.intermediateStops().size();
                    int nextRow = row + 2;

                    if (intermediateStopsCount > 0) {
                        GridPane.setRowSpan(iconImage, 2);
                        this.add(iconImage, 0, row + 1);
                        // Accordion for displaying intermediate stops
                        Accordion accordion = createIntermediateStopsAccordion(transport);
                        GridPane.setColumnSpan(accordion, 2);
                        this.add(accordion, 2, nextRow);
                        nextRow++;
                    } else {
                        this.add(iconImage, 0, row + 1);
                    }

                    Text arrTimeText = new Text(FormatterFr.formatTime(transport.arrTime()));
                    this.add(arrTimeText, 0, nextRow);
                    GridPane.setHalignment(arrTimeText, HPos.RIGHT);

                    Circle arrCircle = new Circle(3, Color.BLACK);
                    this.add(arrCircle, 1, nextRow);

                    Text arrStationText = new Text(transport.arrStop().name());
                    this.add(arrStationText, 2, nextRow);

                    Text arrPlatformText = new Text(transport.arrStop().platformName() != null ?
                            FormatterFr.formatPlatformName(transport.arrStop()) : "");
                    this.add(arrPlatformText, 3, nextRow);

                    circles.add(new Circle[]{depCircle, arrCircle});

                    row = nextRow + 1;
                }
            }
        }

        /**
         * Builds an accordion showing all intermediate stops for a transport leg.
         *
         * @param transport the transport leg containing intermediate stops
         * @return an Accordion that lists each intermediate stop with arrival and departure times
         */
        private static Accordion createIntermediateStopsAccordion(Journey.Leg.Transport transport) {
            int stopsCount = transport.intermediateStops().size();
            long durationMinutes = transport.duration().toMinutes();

            GridPane stopsGrid = new GridPane();
            stopsGrid.getStyleClass().add("intermediate-stops");

            for (int i = 0; i < stopsCount; i++) {
                Journey.Leg.IntermediateStop stop = transport.intermediateStops().get(i);

                Text arrTime = new Text(FormatterFr.formatTime(stop.arrTime()));
                stopsGrid.add(arrTime, 0, i);

                Text depTime = new Text(FormatterFr.formatTime(stop.depTime()));
                stopsGrid.add(depTime, 1, i);

                Text stopName = new Text(stop.stop().name());
                stopsGrid.add(stopName, 2, i);
            }

            String title = String.format("%d arrêt%s, %d min",
                    stopsCount, stopsCount > 1 ? "s" : "",
                    durationMinutes);

            TitledPane titledPane = new TitledPane(title, stopsGrid);
            titledPane.setExpanded(false);

            Accordion accordion = new Accordion();
            accordion.getPanes().add(titledPane);
            return accordion;
        }

        /**
         * Draws red lines between the departure and arrival circles of each transport leg,
         * after the layout of this pane has been computed. These lines are added to the
         * annotations pane to visually connect the journey segments.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            annotations.getChildren().clear();

            for (Circle[] pair : circles) {
                Circle departureCircle = pair[0];
                Circle arrivalCircle = pair[1];


                double startX = departureCircle.getBoundsInParent().getCenterX();
                double startY = departureCircle.getBoundsInParent().getCenterY();
                double endX = arrivalCircle.getBoundsInParent().getCenterX();
                double endY = arrivalCircle.getBoundsInParent().getCenterY();

                Line line = new Line(startX, startY, endX, endY);
                line.setStroke(Color.RED);
                line.setStrokeWidth(2);
                line.setMouseTransparent(true);

                annotations.getChildren().add(line);
            }

        }
    }

    /**
     * Configures the map button to open the selected journey as a GeoJSON in the browser using UMap.
     * If no journey is selected, the button does nothing.
     *
     * @param mapButton the button that triggers the map export
     * @param journeyObservable the observable holding the currently selected journey
     */
    private static void configureMapButton(Button mapButton, ObservableValue<Journey> journeyObservable) {
        mapButton.setOnAction(e -> {
            Journey j = journeyObservable.getValue();
            if (j != null) {
                try {
                    String geoJson = JourneyGeoJsonConverter.toGeoJson(j).toString();
                    geoJson = geoJson.replaceAll("\\s+", "");
                    URI url = new URI(
                            "https",
                            "umap.osm.ch",
                            "/fr/map/",
                            "data=" + geoJson,
                            null);
                    Desktop.getDesktop().browse(url);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    /**
     * Configures the calendar button to export the selected journey to an iCalendar (.ics) file.
     * If no journey is selected, the button does nothing.
     *
     * @param calendarButton the button that triggers the calendar export
     * @param journeyObservable the observable holding the currently selected journey
     */
    private static void configureCalendarButton(Button calendarButton, ObservableValue<Journey> journeyObservable) {
        calendarButton.setOnAction(e -> {
            Journey j = journeyObservable.getValue();
            if (j != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Sauvegarder le voyage");
                fileChooser
                        .getExtensionFilters()
                        .add(new FileChooser
                                .ExtensionFilter(
                                "Fichiers iCalendar (*.ics)",
                                "*.ics"));
                String fileName = "voyage_" + j.depTime().toLocalDate() + ".ics";
                fileChooser.setInitialFileName(fileName);
                File selectedFile = fileChooser.showSaveDialog(null);

                if (selectedFile != null) {
                    try {
                        String icsContent = JourneyIcalConverter.toIcalendar(j);
                        Files.writeString(selectedFile.toPath(),
                                icsContent,
                                StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

}