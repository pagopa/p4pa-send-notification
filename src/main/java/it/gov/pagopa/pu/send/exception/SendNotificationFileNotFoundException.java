package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

public class SendNotificationFileNotFoundException extends BaseBusinessException {

  public SendNotificationFileNotFoundException(String message){
    super(ErrorCodeConstants.ERROR_CODE_FILE_NOT_FOUND, message);
  }
}
