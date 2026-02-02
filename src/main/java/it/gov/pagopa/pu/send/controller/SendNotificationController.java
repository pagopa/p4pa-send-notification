package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactCategoryDTO;
import it.gov.pagopa.pu.send.controller.generated.NotificationApi;
import it.gov.pagopa.pu.send.dto.generated.*;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.service.SendNotificationService;
import it.gov.pagopa.pu.send.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    log.info("New notification request - organizationId={} - payments={}",
      createNotificationRequest.getOrganizationId(),
      Optional.ofNullable(createNotificationRequest.getRecipients())
        .map(recipients -> recipients.stream()
          .flatMap(r -> r.getPayments().stream())
          .map(p -> {
            List<String> details = new ArrayList<>();
            if (p.getPagoPa() != null) {
              details.add("PagoPa[noticeCode=" + p.getPagoPa().getNoticeCode() + "]");
            }
            if (p.getF24() != null) {
              details.add("F24[title=" + p.getF24().getTitle() + "]");
            }
            return String.join(" ", details);
          })
          .filter(s -> !s.isEmpty())
          .toList()
        )
        .orElse(List.of())
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
  public ResponseEntity<SendNotificationDTO> findSendNotificationByOrgIdAndNav(Long organizationId, String nav) {
    log.info("Retrieving sendNotification  having organizationId {} and nav {}" , organizationId, nav);
    return ResponseEntity.ok(sendNotificationService.findSendNotificationByOrgIdAndNav(organizationId, nav));
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

  @Override
  public ResponseEntity<SendNotificationDTO> getSendNotificationByNotificationRequestId(String notificationRequestId) {
    log.info("Retrieving send notification by notificationRequestId {}", notificationRequestId);
    return ResponseEntity.ok(sendNotificationService.findSendNotificationDTOByNotificationRequestId(notificationRequestId));
  }

  @Override
  public ResponseEntity<Void> updateNotificationStatus(Long sendNotificationId, String status) {
    sendNotificationService.updateNotificationStatus(String.valueOf(sendNotificationId), NotificationStatus.valueOf(status));
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> uploadSendLegalFact(String sendNotificationId, LegalFactCategoryDTO category, String fileName, MultipartFile legalFactFile) {
    log.info("Upload legal-fact having category {} for sendNotificationId {}", category, sendNotificationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
