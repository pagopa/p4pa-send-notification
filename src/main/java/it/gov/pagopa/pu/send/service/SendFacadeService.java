package it.gov.pagopa.pu.send.service;


import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;

public interface SendFacadeService {
  void preloadFiles(String sendNotificationId);
  void uploadFiles(String sendNotificationId);
  void deliveryNotification(String sendNotificationId);
  SendNotificationDTO retrieveNotificationData(String sendNotificationId);
  SendNotificationDTO notificationStatus(String sendNotificationId);
}
