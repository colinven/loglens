package loglens.dto;

import java.time.Instant;

public record LogEntry(
        Instant timestamp,
        HttpStatus status,
        HttpMethod method,
        String uri,
        String ip,
        long bytesSent
) {
}
