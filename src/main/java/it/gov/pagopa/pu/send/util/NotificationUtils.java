package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.exception.InvalidStatusException;

public class NotificationUtils {

  private NotificationUtils(){}

  public static <T extends Enum<T>> void validateStatus(T expectedStatus, T actualStatus){
    if(!expectedStatus.equals(actualStatus))
      throw new InvalidStatusException(expectedStatus, actualStatus);
  }
}
