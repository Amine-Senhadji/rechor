package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Graphical component representing the journey query interface.
 * <p>
 * Provides fields for selecting the departure and arrival stops, journey date, and departure time.
 * Automatically binds user input fields to observable values for use by other components.
 * </p>
 *
 * @param rootNode the root UI node containing all visual input components
 * @param depStopO observable representing the selected departure stop
 * @param arrStopO observable representing the selected arrival stop
 * @param dateO observable representing the selected journey date
 * @param timeO observable representing the selected journey departure time
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 * @version 1.0
 * @since 1.0
 */

public record QueryUI(Node rootNode,
                      ObservableValue<String> depStopO,
                      ObservableValue<String> arrStopO,
                      ObservableValue<LocalDate> dateO,
                      ObservableValue<LocalTime> timeO) {
    /** CSS style class name for horizontal containers. */
    private static final String STYLE_HBOX = "HBox";
    /** CSS style class name for date picker labels. */
    private static final String STYLE_DATE = "date";
    /** CSS style class name for time input labels. */
    private static final String STYLE_TIME = "time";

    /** Text for the departure stop label. */
    private static final String LABEL_DEPART = "Départ\u202f:";
    /** Text for the arrival stop label. */
    private static final String LABEL_ARRIVEE = "Arrivée\u202f:";
    /** Text for the date label. */
    private static final String LABEL_DATE = "Date\u202f:";
    /** Text for the time label. */
    private static final String LABEL_HEURE = "Heure\u202f:";

    /** Prompt text shown in the departure stop field when empty. */
    private static final String PROMPT_DEPART = "Arrêt de départ";
    /** Prompt text shown in the arrival stop field when empty. */
    private static final String PROMPT_ARRIVEE = "Arrêt d'arrivée";
    /**
     * Creates the query panel UI for inputting journey parameters.
     *
     * @param stopIndex index mapping stop names and aliases for auto-completion
     * @return a new QueryUI instance with bound input controls
     */
    public static QueryUI create(StopIndex stopIndex){
        // Create root vertical layout and apply stylesheet for styling
        VBox vBox = new VBox();
        vBox.getStylesheets().add("query.css");

        HBox windowHBox = new HBox();
        windowHBox.getStyleClass().add(STYLE_HBOX);

        Label depLabel = new Label(LABEL_DEPART);
        StopField depField = StopField.create(stopIndex);
        depField.textField().setId("depStop");
        depField.textField().setPromptText(PROMPT_DEPART);

        Label arrLabel = new Label(LABEL_ARRIVEE);
        StopField arrField = StopField.create(stopIndex);
        arrField.textField().setPromptText(PROMPT_ARRIVEE);

        // Button used to swap the content of the departure and arrival stop fields
        Button exchangeButton = new Button("↔\uFE0F");
        exchangeButton.setOnAction(event -> {
            String departure = depField.stopO().getValue();
            String arrival = arrField.stopO().getValue();
            depField.setTo(arrival);
            arrField.setTo(departure);
        });

        windowHBox
                .getChildren()
                .addAll(
                        depLabel,
                        depField.textField(),
                        exchangeButton,
                        arrLabel,
                        arrField.textField());

        HBox calendarHBox = new HBox();
        calendarHBox.getStyleClass().add(STYLE_HBOX);

        Label dateLabel = new Label(LABEL_DATE);
        DatePicker datePicker = new DatePicker(LocalDate.now());
        dateLabel.getStyleClass().add(STYLE_DATE);

        Label timeLabel = new Label(LABEL_HEURE);
        TextField timeText = new TextField();
        timeLabel.getStyleClass().add(STYLE_TIME);

        DateTimeFormatter showFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("[H:mm][HH:mm]");
        LocalTimeStringConverter converter = new LocalTimeStringConverter(
                showFormatter, parseFormatter);
        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(converter, LocalTime.now());
        timeText.setTextFormatter(timeFormatter);

        calendarHBox.getChildren().addAll(dateLabel,datePicker,timeLabel,timeText);

        vBox.getChildren().addAll(windowHBox,calendarHBox);
        return new QueryUI(
                vBox,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty()
        );

    }
}