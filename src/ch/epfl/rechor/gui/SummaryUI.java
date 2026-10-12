package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Vehicle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Graphical component that displays a list of journey summaries
 * and allows the user to select one.
 * Each item in the list shows departure/arrival time, route, vehicle icon,
 * and a visual summary of changes. The component automatically selects
 * the journey closest to the given target departure time.
 *
 * @param rootNode the root JavaFX node containing the summary list view
 * @param selectedJourneyO observable representing the currently selected journey
 * @version 1.0
 * @since 1.0
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    /**
     * Creates the summary UI component that displays a list of journeys and
     * automatically selects the one closest to the target departure time.
     *
     * @param journeysO observable list of journey candidates
     * @param depTimeO observable representing the target departure time
     * @return a configured SummaryUI instance
     */
    public static SummaryUI create(
            ObservableValue<List<Journey>> journeysO,
            ObservableValue<LocalTime> depTimeO) {

        ListView<Journey> listView = new ListView<>();
        String css = Objects.requireNonNull(
                SummaryUI.class.getResource("/summary.css")
        ).toExternalForm();
        listView.getStylesheets().add(css);
        listView.setCellFactory(lv -> new SummaryCell());

        // Subscribe to changes in the journey list
        journeysO.subscribe(newJ -> {
            List<Journey> items = (newJ == null) ? List.of() : newJ;
            listView.getItems().setAll(items);
            selectBest(items, depTimeO.getValue(), listView);
        });


        depTimeO.subscribe(newT -> selectBest(listView.getItems(), newT, listView));


        if (journeysO.getValue() != null) {
            List<Journey> items = journeysO.getValue();
            listView.getItems().setAll(items);
            selectBest(items, depTimeO.getValue(), listView);
        }

        return new SummaryUI(listView, listView.getSelectionModel().selectedItemProperty());
    }

    /**
     * Selects the journey whose departure time is the closest to but not before the given target.
     * If no such journey exists, selects the last one.
     *
     * @param items list of journey candidates
     * @param target target departure time
     * @param listView the ListView to update selection and scroll position
     */
    private static void selectBest(List<Journey> items, LocalTime target, ListView<Journey> listView) {
        if (items.isEmpty() || target == null) return;
        int idx = 0;
        while (idx < items.size() &&
                items.get(idx).depTime().toLocalTime().isBefore(target)) {
            idx++;
        }
        if (idx >= items.size()) idx = items.size() - 1;
        listView.getSelectionModel().select(idx);
        int finalIdx = idx;
        Platform.runLater(() -> listView.scrollTo(finalIdx));
    }

    /**
     * Custom cell for displaying a journey summary with vehicle icon, route, times, and change overview.
     */
    private static class SummaryCell extends ListCell<Journey> {
        private final VBox root = new VBox(5);
        private final ImageView iconView = new ImageView();
        private final Text lineAndDest = new Text();
        private final Text depTimeText = new Text();
        private final ChangeLinePane changeLinePane = new ChangeLinePane();
        private final Text arrTimeText = new Text();

        public SummaryCell() {
            iconView.setFitWidth(20);
            iconView.setFitHeight(20);
            depTimeText.getStyleClass().add("departure");
            arrTimeText.getStyleClass().add("arrival");

            HBox topBox = new HBox(5);
            topBox.setAlignment(Pos.CENTER_LEFT);
            topBox.getChildren().setAll(iconView, lineAndDest);

            HBox bottomBox = new HBox(10);
            bottomBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(changeLinePane, Priority.ALWAYS);
            bottomBox.getChildren().setAll(depTimeText, changeLinePane, arrTimeText);

            root.setAlignment(Pos.CENTER_LEFT);
            root.getChildren().setAll(topBox, bottomBox);
        }

        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);

            if (empty || journey == null) {
                setGraphic(null);
            } else {
                Journey.Leg firstLeg = journey.legs().stream()
                        .filter(l -> l instanceof Journey.Leg.Transport)
                        .findFirst().orElse(null);
                if (firstLeg instanceof Journey.Leg.Transport tr) {
                    String veh = tr.vehicle().name().toLowerCase();
                    Vehicle vehicle = tr.vehicle();
                    Image img = VehicleIcons.iconFor(vehicle);
                    iconView.setImage(img);
                    lineAndDest.setText(tr.route() + " Direction " + tr.destination());
                } else {
                    iconView.setImage(null);
                    lineAndDest.setText("");
                }

                depTimeText.setText(formatTimeWithH(journey.depTime().toLocalTime()));
                arrTimeText.setText(formatTimeWithH(journey.arrTime().toLocalTime()));

                changeLinePane.update(journey);

                setGraphic(root);
            }
        }

        /**
         * Formats a LocalTime into a string using an 'h' between hours and minutes.
         * For example, 9:05 becomes "9h05".
         *
         * @param time the LocalTime to format
         * @return formatted time string
         */
        private static String formatTimeWithH(LocalTime time) {
            return time.getHour() + "h" + String.format("%02d", time.getMinute());
        }
    }

    /**
     * Pane displaying a horizontal line with departure/arrival circles,
     * intermediate transfer indicators, and total journey duration.
     */
    private static class ChangeLinePane extends Pane {
        /** Horizontal margin in pixels around the transfer line. */
        private static final double MARGE = 5;
        /** Radius in pixels of the circles marking departure, arrival, and transfers. */
        private static final double RADIUS = 3;
        private final Line line = new Line();
        private final Circle depC = new Circle(RADIUS, Color.BLACK);
        private final Circle arrC = new Circle(RADIUS, Color.BLACK);
        private final Text durationText = new Text();
        private final List<Circle> changes = new java.util.ArrayList<>();

        public ChangeLinePane() {
            setPrefSize(0, 40);
            setMinHeight(40);
            getChildren().addAll(line, depC, arrC, durationText);
        }

        public void update(Journey j) {
            getChildren().removeAll(changes);
            changes.clear();
            Duration total = Duration.between(j.depTime(), j.arrTime());
            List<Journey.Leg> legs = j.legs();
            for (int i = 1; i < legs.size() - 1; i++) {
                if (legs.get(i) instanceof Journey.Leg.Foot foot
                        && legs.get(i - 1) instanceof Journey.Leg.Transport
                        && legs.get(i + 1) instanceof Journey.Leg.Transport) {
                    Duration since = Duration.between(j.depTime(), foot.depTime());
                    double frac = since.toMillis() / (double) total.toMillis();
                    Circle c = new Circle(RADIUS, Color.WHITE);
                    c.getStyleClass().add("transfer");
                    c.setUserData(frac);
                    changes.add(c);
                }
            }
            getChildren().addAll(changes);

            Duration d = Duration.between(j.depTime(), j.arrTime());
            long minutes = d.toMinutes();
            if (minutes < 60) {
                durationText.setText(minutes + " min");
            } else {
                durationText.setText(String.format("%dh %02d min", minutes / 60, minutes % 60));
            }
            durationText.getStyleClass().setAll("summary-duration");
        }

        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            double y = h / 2;

            line.setStartX(MARGE); line.setStartY(y);
            line.setEndX(w - MARGE); line.setEndY(y);
            line.setStrokeWidth(1);
            line.setStroke(Color.GRAY);

            depC.setCenterX(MARGE); depC.setCenterY(y);
            arrC.setCenterX(w - MARGE); arrC.setCenterY(y);

            for (Circle c : changes) {
                double frac = (double) c.getUserData();
                c.setCenterX(MARGE + frac * (w - 2 * MARGE));
                c.setCenterY(y);
            }

            durationText.autosize();
            double dtW = durationText.getLayoutBounds().getWidth();
            durationText.relocate((w - dtW) / 2, y + RADIUS + 2);
        }
    }
}
