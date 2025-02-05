package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NotificationUtilsTest {

  @Test
  void givenExpectedStateWhenValidateStatusThenNoExceptions() {
    Assertions.assertDoesNotThrow(() -> NotificationUtils.validateStatus(NotificationStatus.COMPLETE, NotificationStatus.COMPLETE));
  }

  @Test
  void givenUnexpectedWhenValidateStatusThenNoExceptions() {
    Assertions.assertThrows(InvalidStatusException.class, () -> NotificationUtils.validateStatus(NotificationStatus.COMPLETE, NotificationStatus.REGISTERED));
  }
}
