package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

public interface SendNotificationPaidService {
  SendNotificationNoPII handlePaidNotification(SendNotificationNoPII notification);
}
