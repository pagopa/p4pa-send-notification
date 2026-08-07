package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.exception.common.NotFoundException;
import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

@SuppressWarnings("java:S110") // Suppress "Inheritance tree of classes should not be too deep": allowed for exception hierarchy
public class SendNotificationNotFoundException extends NotFoundException {

  public SendNotificationNotFoundException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_NOTIFICATION_NOT_FOUND, message);
  }
}
