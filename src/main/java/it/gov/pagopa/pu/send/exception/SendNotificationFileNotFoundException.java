package it.gov.pagopa.pu.send.exception;

public class SendNotificationFileNotFoundException extends RuntimeException {
  public SendNotificationFileNotFoundException(String message){
    super(message);
  }
}
