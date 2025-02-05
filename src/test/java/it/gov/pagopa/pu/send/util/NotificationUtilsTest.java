package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NotificationUtilsTest {

  enum Status {
    SUCCESS,
    FAILURE
  }

  @Test
  void givenExpectedStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertDoesNotThrow(() -> NotificationUtils.validateStatus(NotificationStatus.COMPLETE, NotificationStatus.COMPLETE));
  }

  @Test
  void givenUnexpectedNotificationStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(InvalidStatusException.class, () -> NotificationUtils.validateStatus(NotificationStatus.COMPLETE, NotificationStatus.REGISTERED));
  }

  @Test
  void givenUnexpectedFileStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(InvalidStatusException.class, () -> NotificationUtils.validateStatus(FileStatus.READY, FileStatus.UPLOADED));
  }

  @Test
  void givenUnexpectedGenericStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(InvalidStatusException.class, () -> NotificationUtils.validateStatus(Status.SUCCESS, Status.FAILURE));
  }
}
