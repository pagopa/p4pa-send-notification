package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;

public class InvalidStatusException extends RuntimeException{
  public InvalidStatusException(Enum<?> expected, Enum<?> actual) {
    super(buildMessage(expected, actual));
  }

  public InvalidStatusException(String message){super(message);}

  private static String buildMessage(Enum<?> expected, Enum<?> actual) {
    if (expected instanceof FileStatus) {
      return String.format("[INVALID_NOTIFICATION_STATUS] Notification file status error: Expected: %s, Actual: %s", expected, actual);
    } else if (expected instanceof NotificationStatus) {
      return String.format("[INVALID_NOTIFICATION_STATUS] Notification status error: Expected: %s, Actual: %s", expected, actual);
    } else {
      return String.format("[INVALID_NOTIFICATION_STATUS] Notification generic status error: Expected: %s, Actual: %s", expected, actual);
    }
  }
}
