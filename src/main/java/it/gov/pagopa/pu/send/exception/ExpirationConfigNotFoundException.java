package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

public class ExpirationConfigNotFoundException extends BaseBusinessException {

  public ExpirationConfigNotFoundException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_EXPIRATION_CONFIG_NOT_FOUND, message);
  }
}
