package it.gov.pagopa.pu.send.service;

public interface SendFacadeService {
  void preloadFiles(String sendNotificationId);
  void uploadFiles(String sendNotificationId);
  void deliveryNotification(String sendNotificationId);
}
