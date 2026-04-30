package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.util.ErrorCodeConstants;

public class StatusAlreadyProcessedException extends InvalidStatusException {

  public StatusAlreadyProcessedException(Enum<?> expected, Enum<?> actual) {
    super(ErrorCodeConstants.ERROR_CODE_STATUS_ALREADY_PROCESSED, "Expected status is " + expected + ", but it has already be processed: actual is " + actual);
  }
}
