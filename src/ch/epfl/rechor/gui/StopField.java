package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.util.Subscription;

import java.util.List;
import java.util.function.Consumer;
/**
 * UI component for selecting a stop name using auto-completion.
 * <p>
 * This component combines a text field with a suggestion popup that
 * dynamically displays matching stop names as the user types.
 * Subscriptions are used instead of traditional listeners to allow
 * for immediate invocation and controlled lifecycle management.
 * </p>
 *
 * @param textField the underlying text field used for user input
 * @param stopO observable property representing the currently selected stop name
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 * @version 1.0
 * @since 1.0
 */
public record StopField(TextField textField, ObservableValue<String> stopO) {
    /** Maximum number of stop suggestions shown in the list view. */
    private static final int MAX_SUGGESTIONS = 30;

    /** Maximum height of the suggestions popup in pixels. */
    private static final int MAX_LISTVIEW_HEIGHT = 240;

    /**
     * Creates a StopField with auto-completion based on the provided stop index.
     * Subscribes to text, bounds, and focus properties and automatically receives
     * an initial update upon subscription.
     *
     * @param stopIndex index providing stop names and aliases for suggestion
     * @return a new StopField instance with bound behavior
     */
    public static StopField create(StopIndex stopIndex) {
        TextField textField = new TextField();
        ListView<String> listView = new ListView<>();
        listView.setFocusTraversable(false);
        listView.setMaxHeight(MAX_LISTVIEW_HEIGHT);

        Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.getContent().add(listView);

        SimpleStringProperty stopProp = new SimpleStringProperty("");

        // Keyboard navigation in the suggestion list
        textField.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (evt.getCode() == KeyCode.UP && idx > 0) {
                listView.getSelectionModel().select(idx - 1);
                evt.consume();
            } else if (evt.getCode() == KeyCode.DOWN && idx < listView.getItems().size() - 1) {
                listView.getSelectionModel().select(idx + 1);
                evt.consume();
            } else if (evt.getCode() == KeyCode.ENTER) {
                String sel = listView.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    textField.setText(sel);
                    stopProp.set(sel);
                }
                popup.hide();
                evt.consume();
            }
        });

        // Allow mouse-click selection of a stop suggestion
        listView.setOnMouseClicked(evt -> {
            String sel = listView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                textField.setText(sel);
                stopProp.set(sel);
                popup.hide();
            }
        });

        // Subscriber to update suggestions when text changes
        Consumer<String> textSubscriber = newText -> {
            List<String> matches = stopIndex.stopsMatching(newText, MAX_SUGGESTIONS);
            listView.getItems().setAll(matches);
            listView.getSelectionModel().selectFirst();
        };

        // Subscriber to reposition popup when bounds change
        Consumer<Bounds> positionSubscriber = newBound -> {
            Bounds screen = textField.localToScreen(newBound);
            if (screen != null) {
                popup.setAnchorX(screen.getMinX());
                popup.setAnchorY(screen.getMaxY());
            }
        };

        // Hold subscriptions so we can unsubscribe later
        Subscription[] textSub = new Subscription[1];
        Subscription[] posSub = new Subscription[1];

        // Subscriber to show/hide popup on focus changes
        Consumer<Boolean> focusSubscriber = nowFocused -> {
            if (nowFocused) {
                popup.show(textField.getScene().getWindow());
                textSub[0] = textField.textProperty().subscribe(textSubscriber);
                posSub[0] = textField.boundsInLocalProperty().subscribe(positionSubscriber);
            } else {
                popup.hide();
                if (textSub[0] != null) {
                    textSub[0].unsubscribe();
                    textSub[0] = null;
                }
                if (posSub[0] != null) {
                    posSub[0].unsubscribe();
                    posSub[0] = null;
                }
                String sel = listView.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    textField.setText(sel);
                    stopProp.set(sel);
                } else {
                    textField.clear();
                    stopProp.set("");
                }
            }
        };

        // Subscribe to focus with initial invocation
        textField.focusedProperty().subscribe(focusSubscriber);

        return new StopField(textField, stopProp);
    }

    /**
     * Programmatically sets the text field and selected stop value.
     *
     * @param stopName the stop name to set in the field and observable
     */
    public void setTo(String stopName) {
        textField.setText(stopName);
        if (stopO instanceof SimpleStringProperty prop) {
            prop.set(stopName);
        }
    }
}
