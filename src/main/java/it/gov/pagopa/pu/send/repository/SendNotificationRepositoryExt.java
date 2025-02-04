package it.gov.pagopa.pu.send.repository;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.model.SendNotification;

public interface SendNotificationRepositoryExt {

  SendNotification createIfNotExists(Long sendNotificationId, CreateNotificationRequest notificationRequest);
}
