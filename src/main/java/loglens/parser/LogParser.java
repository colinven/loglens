package loglens.parser;

import loglens.dto.LogEntry;

import java.util.Optional;

public interface LogParser {

    boolean matches(String logline);
    Optional<LogEntry> parseLogEntry(String logLine);
}
