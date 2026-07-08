package loglens.parser;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

public class CombinedLogParserTest {

    private final CombinedLogParser parser = new CombinedLogParser();

    // Valid Combined entries — these should all pass
    private final String[] validEntries = {
            "127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326 \"http://www.example.com/start.html\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\"",
            "192.168.1.50 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521 \"https://google.com/\" \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\"",
            "10.0.0.1 - - [12/Jan/2025:08:16:01 +0000] \"GET /page.html HTTP/1.1\" 200 1200 \"-\" \"-\"",
            "203.0.113.42 - - [12/Jan/2025:09:30:45 +0000] \"POST /api/login HTTP/1.1\" 401 89 \"-\" \"curl/7.68.0\"",
            "198.51.100.7 - - [12/Jan/2025:10:05:33 +0000] \"GET /cached.css HTTP/1.1\" 304 - \"https://site.com/home\" \"Mozilla/5.0\"",
            "172.16.0.99 - admin [12/Jan/2025:11:20:14 +0000] \"GET /search?q=test&page=2 HTTP/1.1\" 200 15234 \"https://site.com/search\" \"Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1\"",
            "10.10.10.10 - - [12/Jan/2025:12:00:00 +0000] \"HEAD /status HTTP/1.0\" 500 0 \"-\" \"Googlebot/2.1 (+http://www.google.com/bot.html)\"",
            "192.168.0.1 - bob [12/Jan/2025:13:45:10 +0000] \"DELETE /api/session/abc123 HTTP/1.1\" 204 0 \"https://app.site.com/dashboard\" \"PostmanRuntime/7.28.0\""
    };

    // Invalid Combined entries — these should all fail
    private final String[] invalidEntries = {
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521 \"https://ref.com\"",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521 https://ref.com Mozilla/5.0",
            "127.0.0.1 - - \"GET /index.html HTTP/1.1\" 200 4521 \"-\" \"-\"",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 20 4521 \"-\" \"-\"",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 2000 4521 \"-\" \"-\"",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 abc \"-\" \"-\"",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"-\" 400 0 \"-\" \"-\"",
            "127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521 \"-\" \"Mozilla/5.0\" extra garbage",
            "GARBAGE 127.0.0.1 - - [12/Jan/2025:08:15:22 +0000] \"GET /index.html HTTP/1.1\" 200 4521 \"-\" \"-\""
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
