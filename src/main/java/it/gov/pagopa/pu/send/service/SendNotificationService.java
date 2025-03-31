package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.*;

public interface SendNotificationService {

  CreateNotificationResponse createSendNotification(CreateNotificationRequest createNotificationRequest, String accessToken);
  StartNotificationResponse startSendNotification(String sendNotificationId, LoadFileRequest loadFileRequest, String accessToken);
  void deleteSendNotification(String sendNotificationId);
  SendNotificationDTO findSendNotificationDTO(String sendNotificationId);
}
