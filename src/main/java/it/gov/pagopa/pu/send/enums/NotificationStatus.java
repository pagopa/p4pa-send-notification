package it.gov.pagopa.pu.send.enums;

public enum NotificationStatus {
  // Keep ordered based on entity life cycle https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1460568200/Integrazione+SEND#Data-layer
  // The order is used to verify if a status has already be processed
  WAITING_FILE,
  SENDING,
  REGISTERED,
  UPLOADED,
  // Notification status
  IN_VALIDATION,
  ACCEPTED,
  REFUSED,
  DELIVERING,
  DELIVERED,
  VIEWED,
  EFFECTIVE_DATE,
  PAID,
  UNREACHABLE,
  CANCELLED,
  RETURNED_TO_SENDER
}
