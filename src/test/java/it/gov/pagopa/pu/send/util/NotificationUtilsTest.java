package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.enums.FileStatus;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.exception.StatusAlreadyProcessedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NotificationUtilsTest {

  enum Status {
    SUCCESS,
    FAILURE
  }

  @Test
  void givenExpectedStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertDoesNotThrow(() -> NotificationUtils.validateStatus(NotificationStatus.IN_VALIDATION, NotificationStatus.IN_VALIDATION));
  }

  @Test
  void givenUnexpectedPreviousNotificationStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(InvalidStatusException.class, () -> NotificationUtils.validateStatus(NotificationStatus.IN_VALIDATION, NotificationStatus.REGISTERED));
  }

  @Test
  void givenUnexpectedNextNotificationStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(StatusAlreadyProcessedException.class, () -> NotificationUtils.validateStatus(NotificationStatus.IN_VALIDATION, NotificationStatus.ACCEPTED));
  }

  @Test
  void givenUnexpectedPreviousFileStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(InvalidStatusException.class, () -> NotificationUtils.validateStatus(FileStatus.READY, FileStatus.WAITING));
  }

  @Test
  void givenUnexpectedNextFileStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(StatusAlreadyProcessedException.class, () -> NotificationUtils.validateStatus(FileStatus.READY, FileStatus.UPLOADED));
  }

  @Test
  void givenUnexpectedGenericStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(InvalidStatusException.class, () -> NotificationUtils.validateStatus(Status.SUCCESS, Status.FAILURE));
  }
}
