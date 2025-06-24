package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.NotificationApi;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.service.SendNotificationService;
import it.gov.pagopa.pu.send.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class SendNotificationController implements NotificationApi {

  private final SendNotificationService sendNotificationService;

  public SendNotificationController(
    SendNotificationService sendNotificationService) {
    this.sendNotificationService = sendNotificationService;
  }

  @Override
  public ResponseEntity<CreateNotificationResponse> createSendNotification(CreateNotificationRequest createNotificationRequest) {
    log.info("new notification request for organizationId {} and nav {}",
      createNotificationRequest.getOrganizationId(),
      Optional.ofNullable(createNotificationRequest.getRecipients())
        .map(recipients -> recipients.stream()
          .map(r -> r.getPayments().stream()
            .map(p->p.getPagoPa().getNoticeCode())).toList())
        .orElse(null)
    );
    String accessToken = SecurityUtils.getAccessToken();
    return new ResponseEntity<>(sendNotificationService.createSendNotification(createNotificationRequest, accessToken), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deleteSendNotification(String sendNotificationId) {
    log.info("delete notification request for sendNotificationId {}", sendNotificationId);
    sendNotificationService.deleteSendNotification(sendNotificationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<StartNotificationResponse> startNotification(String sendNotificationId, LoadFileRequest loadFileRequest) {
    log.info("start notification request for sendNotificationId {}", sendNotificationId);
    String accessToken = SecurityUtils.getAccessToken();
    StartNotificationResponse response = sendNotificationService.startSendNotification(sendNotificationId, loadFileRequest, accessToken);
    if (response != null)
      return new ResponseEntity<>(response, HttpStatus.OK);
    else
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @Override
  public ResponseEntity<SendNotificationDTO> getSendNotification(String sendNotificationId) {
    log.info("Retrieving sendNotificationId {}", sendNotificationId);
    return ResponseEntity.ok(sendNotificationService.findSendNotificationDTO(sendNotificationId));
  }
}
