package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * IcalBuilder is a utility class for building iCalendar (RFC 5545) formatted strings.
 * It allows the creation of calendar components such as events and properties in a structured way.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */

public final class IcalBuilder {
    public enum Component {
        VCALENDAR,
        VEVENT
    }

    public enum Name {
        BEGIN,
        END,
        PRODID,
        VERSION,
        UID,
        DTSTAMP,
        DTSTART,
        DTEND,
        SUMMARY,
        DESCRIPTION
    }

    private final StringBuilder icalBuilder = new StringBuilder();
    private static final String CRLF = "\r\n";
    private static final int MAX_LINE_LENGTH = 75;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private final List<Component> components = new ArrayList<>();

    /**
     * Adds a property line to the iCalendar content.
     *
     * @param name  the property name
     * @param value the property value
     * @return the current instance of IcalBuilder for method chaining
     */
    public IcalBuilder add(Name name, String value) {
        // Build the line in the format "NAME:value"
        String line = name.name() + ":" + value;
        // Append the folded version of the line to the iCalendar content
        icalBuilder.append(foldLine(line));
        return this;
    }

    /**
     * Adds a date-time property to the iCalendar content.
     *
     * @param name     the property name
     * @param dateTime the date-time value of the property
     * @return the current instance of IcalBuilder for method chaining
     */
    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        String formattedDate = dateTime.format(DATE_TIME_FORMATTER);
        return add(name, formattedDate);
    }

    /**
     * Begins a new iCalendar component.
     *
     * @param component the component to begin
     * @return the current instance of IcalBuilder for method chaining
     */
    public IcalBuilder begin(Component component) {
        // Register the component in the list
        components.add(component);
        return add(Name.BEGIN, component.name());
    }

    /**
     * Ends the last opened iCalendar component.
     *
     * @return the current instance of IcalBuilder for method chaining
     */
    public IcalBuilder end() {
        // Verify that the component list is not empty
        Preconditions.checkArgument(!components.isEmpty());
        int lastIndex = components.size() - 1;
        Component component = components.remove(lastIndex);
        return add(Name.END, component.name());
    }

    /**
     * Builds and returns the final iCalendar content.
     *
     * @return the completed iCalendar formatted string
     */
    public String build() {
        // Verify that no component is still open
        Preconditions.checkArgument(components.isEmpty());
        return icalBuilder.toString();
    }

    /**
     * Folds a line according to the iCalendar specifications: if the line exceeds the maximum allowed
     * length, it is broken into multiple lines with a leading space on each continuation line.
     *
     * @param line the line to fold
     * @return the folded line as a string ending with CRLF
     */
    private String foldLine(String line) {
        StringBuilder folded = new StringBuilder();
        // If the line is short enough (<= MAX_LINE_LENGTH), return it as is followed by CRLF.
        if (line.length() <= MAX_LINE_LENGTH) {
            folded.append(line).append(CRLF);
            return folded.toString();
        }

        // Append the first part of the line (up to MAX_LINE_LENGTH characters) and a CRLF.
        folded.append(line, 0, MAX_LINE_LENGTH).append(CRLF);

        // For subsequent lines, add a leading space and break the rest into segments of
        // (MAX_LINE_LENGTH - 1) characters.
        int index = MAX_LINE_LENGTH;
        int subsequentMax = MAX_LINE_LENGTH - 1;
        while (index < line.length()) {
            int endIndex = Math.min(index + subsequentMax, line.length());
            folded.append(" ").append(line, index, endIndex).append(CRLF);
            index = endIndex;
        }

        return folded.toString();
    }
}