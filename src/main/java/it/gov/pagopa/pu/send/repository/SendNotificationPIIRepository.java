package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.dto.SendNotification;

public interface SendNotificationPIIRepository {

  SendNotification save(SendNotification sendNotification);
}
