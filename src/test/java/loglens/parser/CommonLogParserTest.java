package loglens.parser;

import loglens.dto.LogEntry;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class CommonLogParserTest {

    private final CommonLogParser parser = new CommonLogParser();

    // Valid CLF entries — these should all pass
    private final String[] validEntries = {
            "127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326",
            "192.168.1.50 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521",
            "10.0.0.1 - - [12/Jan/2025:08:16:01 +0000] \"GET /cached.css HTTP/1.1\" 304 -",
            "203.0.113.42 - admin [12/Jan/2025:09:30:45 +0000] \"POST /api/v1/users/create HTTP/1.1\" 201 89",
            "198.51.100.7 - - [12/Jan/2025:10:05:33 +0000] \"GET /search?q=test&page=2 HTTP/1.1\" 200 15234",
            "172.16.0.99 - - [12/Jan/2025:11:20:14 +0000] \"GET /missing.html HTTP/1.1\" 404 512",
            "10.10.10.10 - bob [12/Jan/2025:12:00:00 +0000] \"PUT /api/data HTTP/1.1\" 500 1024",
            "192.168.0.1 - - [12/Jan/2025:13:45:10 +0000] \"DELETE /api/session/abc123 HTTP/1.1\" 204 0",
            "192.168.0.2 - - [12/Jan/2025:13:46:00 +0000] \"HEAD /status HTTP/1.0\" 200 -"
    };

    // Invalid CLF entries — these should all fail
    private final String[] invalidEntries = {
            "127.0.0.1 - - \"GET /index.html HTTP/1.1\" 200 4521",
            "127.0.0.1 - - 12/Jan/2025:08:15:22 +0000 \"GET /index.html HTTP/1.1\" 200 4521",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] GET /index.html HTTP/1.1 200 4521",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 20 4521",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 2000 4521",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" OK 4521",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 abc",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 12.5",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"-\" 400 0",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521 extra garbage",
            "GARBAGE 127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521 \"http://ref.com\" \"Mozilla/5.0\""
    };

    /* ————— matches() ————— */

    @Test
    void givenVariousValidEntries_whenMatches_thenAllShouldReturnTrue() {
        SoftAssertions softAssertions = new SoftAssertions();
        for (String entry : validEntries) {
            softAssertions.assertThat(parser.matches(entry)).isTrue();
        }
        softAssertions.assertAll();
    }

    @Test
    void givenVariousInvalidEntries_whenMatches_thenAllShouldReturnFalse() {
        SoftAssertions softAssertions = new SoftAssertions();
        for (String entry : invalidEntries) {
            softAssertions.assertThat(parser.matches(entry)).isFalse();
        }
        softAssertions.assertAll();
    }

    /* ————— parseLogEntry() ————— */

    @Test
    void givenVariousValidEntries_whenParseLogEntry_thenAllShouldReturnNonEmpty() {
        SoftAssertions softAssertions = new SoftAssertions();
        for (String entry : validEntries) {
            softAssertions.assertThat(parser.parseLogEntry(entry)).isNotEmpty();
        }
        softAssertions.assertAll();
    }

    @Test
    void givenVariousInvalidEntries_whenParseLogEntry_thenAllShouldReturnEmpty() {
        SoftAssertions softAssertions = new SoftAssertions();
        for (String entry : invalidEntries) {
            softAssertions.assertThat(parser.parseLogEntry(entry)).isEmpty();
        }
        softAssertions.assertAll();
    }

}
