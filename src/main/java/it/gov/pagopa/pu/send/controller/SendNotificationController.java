package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.NotificationApi;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.send.dto.generated.StartNotificationResponse;
import it.gov.pagopa.pu.send.service.SendNotificationService;
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
  public ResponseEntity<CreateNotificationResponse> createSendNotification(
    CreateNotificationRequest createNotificationRequest) {
    log.info("new notification request");
    return new ResponseEntity<>(sendNotificationService.createSendNotification(createNotificationRequest),HttpStatus.OK);
  }

  @Override
  public ResponseEntity<StartNotificationResponse> startNotification(
    String sendNotificationId, LoadFileRequest loadFileRequest) {
    log.info("start notification request for sendNotificationId {}", sendNotificationId);
    StartNotificationResponse response = sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest);
    if(response!=null)
      return new ResponseEntity<>(response, HttpStatus.OK);
    else
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
