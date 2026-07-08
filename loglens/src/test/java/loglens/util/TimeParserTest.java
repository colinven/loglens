package loglens.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TimeParserTest {

    private final TimeParser timeParser = new TimeParser();

    /* ————— parseAndSubtract() ————— */

    @Test
    void givenDaysHoursAndMinutes_whenParseAndSubtract_thenParsesCorrectly() {
        Instant fixed = Instant.parse("2026-07-07T12:00:00Z");
        Clock clock = Clock.fixed(fixed, ZoneOffset.UTC);

        Instant result = timeParser.parseAndSubtract("2d12h30m", clock.instant());

        assertThat(result).isEqualTo(Instant.parse("2026-07-04T23:30:00Z"));
    }

    @Test
    void givenDaysOnly_whenParseAndSubtract_thenParsesCorrectly() {
        Instant fixed = Instant.parse("2026-07-07T12:00:00Z");
        Clock clock = Clock.fixed(fixed, ZoneOffset.UTC);

        Instant result = timeParser.parseAndSubtract("2d", clock.instant());

        assertThat(result).isEqualTo(Instant.parse("2026-07-05T12:00:00Z"));
    }

    @Test
    void givenHoursOnly_whenParseAndSubtract_thenParsesCorrectly() {
        Instant fixed = Instant.parse("2026-07-07T12:00:00Z");
        Clock clock = Clock.fixed(fixed, ZoneOffset.UTC);

        Instant result = timeParser.parseAndSubtract("2h", clock.instant());

        assertThat(result).isEqualTo(Instant.parse("2026-07-07T10:00:00Z"));
    }

    @Test
    void givenMinutesOnly_whenParseAndSubtract_thenParsesCorrectly() {
        Instant fixed = Instant.parse("2026-07-07T12:00:00Z");
        Clock clock = Clock.fixed(fixed, ZoneOffset.UTC);

        Instant result = timeParser.parseAndSubtract("30m", clock.instant());

        assertThat(result).isEqualTo(Instant.parse("2026-07-07T11:30:00Z"));
    }

    @Test
    void givenZeroValueInput_whenParseAndSubtract_thenMakesNoSubtraction() {
        Instant fixed = Instant.parse("2026-07-07T12:00:00Z");
        Clock clock = Clock.fixed(fixed, ZoneOffset.UTC);

        Instant result = timeParser.parseAndSubtract("0d0h0m", clock.instant());

        assertThat(result).isEqualTo(Instant.parse("2026-07-07T12:00:00Z"));
    }

    @Test
    void givenVariousInvalidInputs_whenParseAndSubtract_thenReturnsNull() {
        Instant instant = Instant.parse("2026-07-07T12:00:00Z");

        Instant result1 = timeParser.parseAndSubtract("2days", instant);
        Instant result2 = timeParser.parseAndSubtract("24hours", instant);
        Instant result3 = timeParser.parseAndSubtract("30minutes", instant);

        assertThat(result1).isNull();
        assertThat(result2).isNull();
        assertThat(result3).isNull();


    }

}
