package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.exception.common.BaseBusinessException;

public class FileAlreadyExistsException extends BaseBusinessException {
  public FileAlreadyExistsException(String code, String message) {
    super(code, message);
  }
}
