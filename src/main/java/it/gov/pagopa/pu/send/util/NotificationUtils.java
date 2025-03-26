package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.exception.InvalidStatusException;
import it.gov.pagopa.pu.send.exception.StatusAlreadyProcessedException;

public class NotificationUtils {

  private NotificationUtils(){}

  public static <T extends Enum<T>> void validateStatus(T expectedStatus, T actualStatus){
    if(!expectedStatus.equals(actualStatus)){
      if(expectedStatus.compareTo(actualStatus) > 0){
        throw new InvalidStatusException(expectedStatus, actualStatus);
      } else {
        throw new StatusAlreadyProcessedException(expectedStatus, actualStatus);
      }
    }
  }
}
