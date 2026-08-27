package it.gov.pagopa.pu.send.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

class DateUtilsTest {

  @Test
  void givenValidDateWhenToLocalDateTimeThenOk() {
    LocalDateTime date = LocalDateTime.of(2025, Month.JANUARY, 16, 9, 15, 20);
    LocalDateTime expectedDate = date.minusHours(4);

    LocalDateTime result = DateUtils.toLocalDateTime(OffsetDateTime.of(date, ZoneOffset.ofHours(5)));

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedDate.getYear(), result.getYear());
    Assertions.assertEquals(expectedDate.getMonth(), result.getMonth());
    Assertions.assertEquals(expectedDate.getDayOfMonth(), result.getDayOfMonth());
    Assertions.assertEquals(expectedDate.getHour(), result.getHour());
    Assertions.assertEquals(expectedDate.getMinute(), result.getMinute());
    Assertions.assertEquals(expectedDate.getSecond(), result.getSecond());
  }

  @Test
  void givenNullDateWhenToLocalDateTimeThenNullResult() {
    LocalDateTime result = DateUtils.toLocalDateTime(null);

    Assertions.assertNull(result);
  }

  @Test
  void givenValidDateWhenToOffsetDateTimeThenOk() {
    Date date = Date.from(Instant.parse("2026-08-26T15:18:19.123Z"));
    OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse("2026-08-26T17:18:19.123+02:00");

    OffsetDateTime actualOffsetDateTime = DateUtils.toOffsetDateTime(date);

    Assertions.assertNotNull(actualOffsetDateTime);
    Assertions.assertEquals(expectedOffsetDateTime, actualOffsetDateTime);
  }

  @Test
  void givenNullDateWhenToOffsetDateTimeThenNull() {
    OffsetDateTime result = DateUtils.toOffsetDateTime(null);

    Assertions.assertNull(result);
  }
}
