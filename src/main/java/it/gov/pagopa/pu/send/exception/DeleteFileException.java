package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

public class DeleteFileException extends BaseBusinessException {

  public DeleteFileException(String message) {
    super(ErrorCodeConstants.ERROR_CODE_DELETE_ERROR, message);
  }
}
