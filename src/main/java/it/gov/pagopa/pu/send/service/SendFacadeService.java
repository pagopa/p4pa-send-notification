package it.gov.pagopa.pu.send.service;


import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;

public interface SendFacadeService {
  void preloadFiles(String sendNotificationId);
  void uploadFiles(String sendNotificationId);
  void deliveryNotification(String sendNotificationId);
  NewNotificationRequestStatusResponseV24DTO notificationStatus(String sendNotificationId);
}
