package loglens.dto;

import java.time.Instant;

public record LogEntry(
        String ip,
        Instant timestamp,
        HttpMethod method,
        String uri,
        HttpStatus status,
        long bytesSent
) {
}
