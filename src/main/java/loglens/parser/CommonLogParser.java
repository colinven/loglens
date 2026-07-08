package loglens.parser;

import loglens.dto.HttpMethod;
import loglens.dto.HttpStatus;
import loglens.dto.LogEntry;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonLogParser implements LogParser {

    // Pattern captures [ IP / TIMESTAMP / HTTP METHOD / REQUEST URI / RESPONSE CODE / BYTE COUNT ]
    private final String regex = "^(\\S+) \\S+ \\S+ \\[(.*?)] \"(\\S+)\\s+(\\S+)\\s+[^\"]*\" (\\d{3}) (\\d+|-)$";
    private final Pattern pattern = Pattern.compile(regex);
    private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("dd/MMM/yyyy:HH:mm:ss ")
            .appendOffset("+HHmm", "Z")
            .toFormatter(Locale.ENGLISH);

    @Override
    public boolean matches(String logline) {
        Matcher matcher = pattern.matcher(logline);
        return matcher.matches();
    }

    @Override
    public Optional<LogEntry> parseLogEntry(String logLine) {

        Matcher matcher = pattern.matcher(logLine);
        if (matcher.matches()) {
            try {
                String ip = matcher.group(1);
                Instant timestamp = Instant.from(formatter.parse(matcher.group(2)));
                HttpMethod method = HttpMethod.valueOf(matcher.group(3));
                String uri = matcher.group(4);
                HttpStatus status = HttpStatus.fromCode(Integer.parseInt(matcher.group(5)));
                long bytesSent = matcher.group(6).equals("-") ? 0 : Long.parseLong(matcher.group(6));

                LogEntry entry = new LogEntry(ip, timestamp, method, uri, status, bytesSent);
                return Optional.of(entry);
            } catch (IllegalArgumentException | DateTimeException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }


}
