package loglens.parser;

import loglens.dto.LogEntry;

import java.util.Optional;

public class CombinedLogParser implements LogParser {

    @Override
    public boolean matches(String logline) {
        return false;
    }

    @Override
    public Optional<LogEntry> parseLogEntry(String logLine) {
        return Optional.empty();
    }
}
