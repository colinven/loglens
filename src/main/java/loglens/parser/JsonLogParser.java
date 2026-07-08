package loglens.parser;

import loglens.dto.LogEntry;

import java.util.Optional;

public class JsonLogParser implements LogParser {

    /**
     * <p>JSON log parsing — DEFERRED.</p>
     *
     * <p>Not implemented yet. Shipping CLF + Combined first; JSON is a follow-up once the
     *  core detect → parse → report pipeline is solid.</p>
     *
     * <p>The full design is already worked out — see:</p>
     *
     *     <p>specs/json_parsing.md</p>
     */

    @Override
    public boolean matches(String logline) {
        return false;
    }

    @Override
    public Optional<LogEntry> parseLogEntry(String logLine) {
        return Optional.empty();
    }
}
