package it.gov.pagopa.pu.send.exception;

public class StatusAlreadyProcessedException extends InvalidStatusException {
  public StatusAlreadyProcessedException(Enum<?> expected, Enum<?> actual) {
    super("[STATUS_ALREADY_PROCESSED] Expected status is " + expected + ", but it has already be processed: actual is " + actual);
  }
}
