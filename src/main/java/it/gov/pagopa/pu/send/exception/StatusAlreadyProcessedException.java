package it.gov.pagopa.pu.send.exception;

public class StatusAlreadyProcessedException extends InvalidStatusException {
  public StatusAlreadyProcessedException(Enum<?> expected, Enum<?> actual) {
    super("Expected status is " + expected + ", but it has already be processed: actual is " + actual);
  }
}
