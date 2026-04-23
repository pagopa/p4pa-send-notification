package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

public class InvalidSignatureException extends BaseBusinessException {

  public InvalidSignatureException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_INVALID_SIGNATURE, message);
  }
}
