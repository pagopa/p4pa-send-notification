package it.gov.pagopa.pu.send.exception;

public class FileAlreadyExistsException extends BaseBusinessException {
  public FileAlreadyExistsException(String code, String message) {
    super(code, message);
  }
}
