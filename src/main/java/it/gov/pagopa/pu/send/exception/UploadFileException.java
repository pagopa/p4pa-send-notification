package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.exception.common.BaseBusinessException;

public class UploadFileException extends BaseBusinessException {
  public UploadFileException(String code, String message){ super(code, message);}
}
