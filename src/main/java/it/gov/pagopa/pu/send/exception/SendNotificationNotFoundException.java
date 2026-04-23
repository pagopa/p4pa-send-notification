package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

public class SendNotificationNotFoundException extends BaseBusinessException {

  public SendNotificationNotFoundException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_NOTIFICATION_NOT_FOUND, message);
  }
}
