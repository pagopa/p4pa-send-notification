package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.dto.SendNotification;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;

public interface SendNotificationPIIRepository {

  SendNotification save(SendNotification sendNotification);
  void delete(SendNotificationNoPII sendNotificationNoPII);
}
