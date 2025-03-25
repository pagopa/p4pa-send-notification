package it.gov.pagopa.pu.send.exception;

public class UnknownDebtPositionException extends RuntimeException {
  public UnknownDebtPositionException(String message){
    super(message);
  }
}
