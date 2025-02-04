package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;

public interface SendNotificationService {

  CreateNotificationResponse createSendNotification(CreateNotificationRequest createNotificationRequest);
}
