package it.gov.pagopa.pu.send.service;

public interface SendService {
  void preloadFiles(String sendNotificationId);
  void uploadFiles(String sendNotificationId);
}
