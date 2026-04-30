package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

public class UnknownDebtPositionException extends BaseBusinessException {

  public UnknownDebtPositionException(String message){
    super(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_NOT_FOUND, message);
  }
}
