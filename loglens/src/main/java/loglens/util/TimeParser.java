package loglens.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

    /**
     * This method parses an input string duration in the format of: 'Xd Yh Zm' where X,Y,Z are integers (e.g. '2d 12h 30m'),
     * subtracts the input duration from the current time, and returns an Instant value representation of that moment.
     * <p>VALID SYNTAX: any combination of: 'd' - days, 'h' hours, 'm' - minutes, preceded by a non-negative integer</p>
     * @param duration time duration to parse
     * @param now current time
     * @return Instant value representation of "now minus duration" or NULL if invalid
     */
    public Instant parseAndSubtract (String duration, Instant now) {

        if (duration == null || duration.isEmpty() || now == null) {
            return null;
        }

        // Pattern matches optional days, hours, and minutes
        Pattern pattern = Pattern.compile("(?:(\\d+)d)?\\s*(?:(\\d+)h)?\\s*(?:(\\d+)m)?");
        Matcher matcher = pattern.matcher(duration);

        if (matcher.matches()) {
            int days = (matcher.group(1) == null) ? 0 : Integer.parseInt(matcher.group(1));
            int hours = (matcher.group(2) == null) ? 0 : Integer.parseInt(matcher.group(2));
            int minutes = (matcher.group(3) == null) ? 0 : Integer.parseInt(matcher.group(3));

            return now
                    .minus(days, ChronoUnit.DAYS)
                    .minus(hours, ChronoUnit.HOURS)
                    .minus(minutes, ChronoUnit.MINUTES);
        }
        return null;
    }

}
