package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.NotificationApi;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.send.dto.generated.StartNotificationResponse;
import it.gov.pagopa.pu.send.service.SendNotificationService;
import it.gov.pagopa.pu.send.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SendNotificationController implements NotificationApi {

  private final SendNotificationService sendNotificationService;

  public SendNotificationController(
    SendNotificationService sendNotificationService) {
    this.sendNotificationService = sendNotificationService;
  }

  @Override
  public ResponseEntity<CreateNotificationResponse> createSendNotification(Long organizationId, CreateNotificationRequest createNotificationRequest) {
    log.info("new notification request for organizationId {}", organizationId);
    return new ResponseEntity<>(sendNotificationService.createSendNotification(createNotificationRequest, organizationId), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deleteSendNotification(String sendNotificationId, Long organizationId) {
    log.info("delete notification request for sendNotificationId {} and organizationId {}", sendNotificationId, organizationId);
    sendNotificationService.deleteSendNotification(sendNotificationId, organizationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<StartNotificationResponse> startNotification(
    String sendNotificationId, Long organizationId, LoadFileRequest loadFileRequest) {
    log.info("start notification request for sendNotificationId {} and organizationId {}", sendNotificationId, organizationId);
    String accessToken = SecurityUtils.getAccessToken();
    StartNotificationResponse response = sendNotificationService.startSendNotification(sendNotificationId, organizationId, loadFileRequest, accessToken);
    if (response != null)
      return new ResponseEntity<>(response, HttpStatus.OK);
    else
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
