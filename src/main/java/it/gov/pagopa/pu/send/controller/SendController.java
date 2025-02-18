package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NewNotificationRequestStatusResponseV24DTO;
import it.gov.pagopa.pu.send.controller.generated.SendApi;
import it.gov.pagopa.pu.send.service.SendFacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SendController implements SendApi {

  private final SendFacadeService sendFacadeService;

  public SendController(SendFacadeService sendFacadeService) {
    this.sendFacadeService = sendFacadeService;
  }

  @Override
  public ResponseEntity<Void> preloadSendFile(String sendNotificationId) {
    log.info("request preload files for sendNotificationId:{}", sendNotificationId);
    sendFacadeService.preloadFiles(sendNotificationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> uploadSendFile(String sendNotificationId) {
    log.info("upload files to SEND safeStorage for sendNotificationId:{}", sendNotificationId);
    sendFacadeService.uploadFiles(sendNotificationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deliveryNotification(String sendNotificationId) {
    log.info("delivery notification with sendNotificationId {} to SEND", sendNotificationId);
    sendFacadeService.deliveryNotification(sendNotificationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<NewNotificationRequestStatusResponseV24DTO> notificationStatus(String sendNotificationId) {
    log.info("retrieve notification status for sendNotificationId {}", sendNotificationId);
    return new ResponseEntity<>(sendFacadeService.notificationStatus(sendNotificationId), HttpStatus.OK);
  }

}
