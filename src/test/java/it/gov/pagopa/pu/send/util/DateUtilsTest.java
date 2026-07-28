package it.gov.pagopa.pu.send.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
}
