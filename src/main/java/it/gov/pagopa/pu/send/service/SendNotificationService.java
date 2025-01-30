package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import java.util.List;

public interface SendNotificationService {

  CreateNotificationResponse createSendNotification(List<CreateNotificationRequest> newNotifications);
}
