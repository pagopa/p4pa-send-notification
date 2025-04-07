package it.gov.pagopa.pu.send.service;


import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPriceResponseV23DTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;

public interface SendFacadeService {
  void preloadFiles(String sendNotificationId);
  void uploadFiles(String sendNotificationId);
  void deliveryNotification(String sendNotificationId);
  SendNotificationDTO retrieveNotificationData(String sendNotificationId);
  SendNotificationDTO notificationStatus(String sendNotificationId);
  NotificationPriceResponseV23DTO retrieveNotificationPrice(Long organizationId, String noticeCode);
}
