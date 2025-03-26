package it.gov.pagopa.pu.send.exception;

public class SendNotificationNotFoundException extends RuntimeException {
  public SendNotificationNotFoundException(String message){
    super(message);
  }
}
