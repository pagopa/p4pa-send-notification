package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.send.dto.generated.StartNotificationResponse;

public interface SendNotificationService {

  CreateNotificationResponse createSendNotification(CreateNotificationRequest createNotificationRequest, Long organizationId, String accessToken);
  StartNotificationResponse startSendNotification(String sendNotificationId, Long organizationId, LoadFileRequest loadFileRequest, String accessToken);
  void deleteSendNotification(String sendNotificationId, Long organizationId);
}
