package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
/**
 * A utility class for formatting various elements related to journeys in French.
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public final class FormatterFr {
    //private constructor to prevent instantiation
    private FormatterFr (){}
    /**
     * DateTimeFormatter for formatting time in "Hhmm" format.
     */
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NEVER)
            .appendLiteral('h')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter();

    /**
     * Formats a duration into a human-readable French format.
     * @param duration The duration to format.
     * @return A formatted string representing the duration.
    */
    public static String formatDuration(Duration duration){
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        minutes = minutes % 60;
        return hours > 0 ? hours + " h " + minutes + " min" : minutes + " min";
    }
    /**
     * Formats a LocalDateTime object into a French time format.
     * @param dateTime The date and time to format.
     * @return A formatted time string.
     */
    public static String formatTime(LocalDateTime dateTime) {
        return TIME_FORMATTER.format(dateTime);
    }
    /**
     * Formats a platform name into "voie X" or "quai X" based on its first character
     * @param stop The stop whose platform name needs formatting.
     * @return A formatted platform name or an empty string if unavailable.
     */
    public static String formatPlatformName(Stop stop) {
        String platform = stop.platformName();
        if (platform == null || platform.isEmpty()) {
            return "";
        }
        String prefix = Character.isDigit(platform.charAt(0)) ? "voie " : "quai ";
        return prefix + platform;
    }
    /**
     * Formats a walking leg of a journey, indicating whether it's a transfer or a walking segment.
     * @param footLeg The walking leg to format.
     * @return A formatted string representing the walking leg.
     */
    public static String formatLeg(Journey.Leg.Foot footLeg) {
        return (footLeg.isTransfer() ? "changement" : "trajet à pied") + " (" + formatDuration(footLeg.duration()) + ")";
    }
    /**
     * Formats the route and destination of a transport leg.
     * @param transport The transport leg to format.
     * @return A formatted string with route and destination.
     */
    public static String formatRouteDestination(Journey.Leg.Transport transport) {
        return transport.route() + " Direction " + transport.destination();
    }
    /**
     * Formats a transport leg, including departure and arrival details.
     * @param leg The transport leg to format.
     * @return A formatted string representing the transport leg.
     */
    public static String formatLeg(Journey.Leg.Transport leg) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatTime(leg.depTime())).append(" ").append(leg.depStop().name());

        String depPlatform = formatPlatformName(leg.depStop());
        if (!depPlatform.isEmpty()) {
            sb.append(" (").append(depPlatform).append(")");
        }

        sb.append(" → ").append(leg.arrStop().name())
          .append(" (arr. ")
          .append(formatTime(leg.arrTime()));

        String arrPlatform = formatPlatformName(leg.arrStop());
        if (!arrPlatform.isEmpty()) {
            sb.append(" ").append(arrPlatform);
        }

        sb.append(")");
        return sb.toString();
    }

}
