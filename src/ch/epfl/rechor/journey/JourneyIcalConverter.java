package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * JourneyIcalConverter provides a utility method to convert a Journey into an iCalendar event
 * string.
 * The resulting iCalendar string adheres to the specified format with a VCALENDAR object
 * containing a single VEVENT.
 *  @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class JourneyIcalConverter {
    // Private constructor to forbid instantiation
    private JourneyIcalConverter() {}

    /**
     * Converts the given Journey into an iCalendar event string.
     * The resulting iCalendar contains:
     * A VCALENDAR component with VERSION (2.0) and PRODID ("ReCHor") attributes.
     * A VEVENT component with a UID (random UUID), DTSTAMP (current time),
     * DTSTART (departure time), DTEND (arrival time) and a SUMMARY composed of the departure
     * and arrival station names.
     * A DESCRIPTION built by concatenating the textual representations of each journey leg,
     *         separated by the two-character sequence "\n".
     *
     * @param journey the journey to convert
     * @return the iCalendar string representing the journey
     */
    public static String toIcalendar(Journey journey) {

        final LocalDateTime dtStamp = LocalDateTime.now();
        // Build the description from the journey legs
        final StringJoiner descriptionJoiner = new StringJoiner("\\n");
        for (Journey.Leg leg : journey.legs()) {
            switch (leg) {
                case Journey.Leg.Foot f -> descriptionJoiner.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t -> descriptionJoiner.add(FormatterFr.formatLeg(t));
            }
        }
        // Open the VCALENDAR component and add its attributes
        return new IcalBuilder()
                .begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.VERSION, "2.0")
                .add(IcalBuilder.Name.PRODID, "ReCHor")
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, UUID.randomUUID().toString())
                .add(IcalBuilder.Name.DTSTAMP, dtStamp)
                .add(IcalBuilder.Name.DTSTART, journey.depTime())
                .add(IcalBuilder.Name.DTEND, journey.arrTime())
                .add(IcalBuilder.Name.SUMMARY,
                        journey.depStop().name() + " → " + journey.arrStop().name())
                .add(IcalBuilder.Name.DESCRIPTION, descriptionJoiner.toString())
                .end() // End VEVENT
                .end() // End VCALENDAR
                .build();
    }
}
