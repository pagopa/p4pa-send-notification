package it.gov.pagopa.pu.send.exception;

public class InvalidSignatureException extends RuntimeException{
  public InvalidSignatureException(String message){ super(message);}
}
