package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;

public class InvalidStatusException extends RuntimeException{
  public InvalidStatusException(Enum<?> expected, Enum<?> actual) {
    super(buildMessage(expected, actual));
  }

  private static String buildMessage(Enum<?> expected, Enum<?> actual) {
    if (expected instanceof FileStatus) {
      return String.format("Notification file status error: Expected: %s, Actual: %s", expected, actual);
    } else if (expected instanceof NotificationStatus) {
      return String.format("Notification status error: Expected: %s, Actual: %s", expected, actual);
    } else {
      return String.format("Notification generic status error: Expected: %s, Actual: %s", expected, actual);
    }
  }
}
