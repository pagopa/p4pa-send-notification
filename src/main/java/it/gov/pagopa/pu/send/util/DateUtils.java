package it.gov.pagopa.pu.send.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import static it.gov.pagopa.pu.send.util.Constants.ZONEID;

public class DateUtils {
  private DateUtils() {
  }

  public static LocalDateTime toLocalDateTime(OffsetDateTime date) {
    return date != null ? date.atZoneSameInstant(ZONEID).toLocalDateTime() : null;
  }

  public static OffsetDateTime toOffsetDateTime(Date date) {
    return date != null ? OffsetDateTime.ofInstant(date.toInstant(), ZONEID) : null;
  }

}
