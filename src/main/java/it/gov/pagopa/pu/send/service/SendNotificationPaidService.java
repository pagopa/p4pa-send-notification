package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

import java.util.Optional;

public interface SendNotificationPaidService {
  Optional<SendNotificationNoPII> handlePaidNotification(SendNotificationNoPII notification);
}
