package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.connector.send.generated.dto.LegalFactCategoryDTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPriceResponseV23DTO;
import it.gov.pagopa.pu.send.controller.generated.SendApi;
import it.gov.pagopa.pu.send.dto.generated.LegalFactDownloadMetadataDTO;
import it.gov.pagopa.pu.send.dto.generated.LegalFactListElementDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.service.SendFacadeService;
import it.gov.pagopa.pu.send.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
public class SendController implements SendApi {

  private final SendFacadeService sendFacadeService;

  public SendController(SendFacadeService sendFacadeService) {
    this.sendFacadeService = sendFacadeService;
  }

  @Override
  public ResponseEntity<Void> preloadSendFile(String sendNotificationId) {
    log.info("request preload files for sendNotificationId {}", sendNotificationId);
    sendFacadeService.preloadFiles(sendNotificationId, SecurityUtils.getAccessToken());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<SendNotificationDTO> retrieveNotificationDate(String sendNotificationId) {
    log.info("retrieve notificationData for sendNotificationId {}", sendNotificationId);
    SendNotificationDTO response = sendFacadeService.retrieveNotificationDate(sendNotificationId, SecurityUtils.getAccessToken());
    if(response!=null)
      return new ResponseEntity<>(response, HttpStatus.OK);
    else
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity<NotificationPriceResponseV23DTO> retrieveNotificationPrice(Long organizationId, String nav) {
    log.info("retrieve notificationPrice for organizationId {} and nav {}", organizationId, nav);
    NotificationPriceResponseV23DTO response = sendFacadeService.retrieveNotificationPrice(organizationId, nav, SecurityUtils.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> uploadSendFile(String sendNotificationId) {
    log.info("upload files to SEND safeStorage for sendNotificationId {}", sendNotificationId);
    sendFacadeService.uploadFiles(sendNotificationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deliveryNotification(String sendNotificationId) {
    log.info("delivery notification with sendNotificationId {} to SEND", sendNotificationId);
    sendFacadeService.deliveryNotification(sendNotificationId, SecurityUtils.getAccessToken());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<SendNotificationDTO> notificationStatus(String sendNotificationId) {
    log.info("retrieve notification status for sendNotificationId {}", sendNotificationId);
    return new ResponseEntity<>(sendFacadeService.notificationStatus(sendNotificationId, SecurityUtils.getAccessToken()), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<List<LegalFactListElementDTO>> retrieveLegalFacts(String sendNotificationId) {
    log.info("retrieve legal fact list for sendNotificationId {} from SEND", sendNotificationId);
    List<LegalFactListElementDTO> response = sendFacadeService.retrieveLegalFacts(sendNotificationId, SecurityUtils.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<LegalFactDownloadMetadataDTO> retrieveLegalFactDownloadMetadata(String sendNotificationId, String legalFactId) {
    log.info("retrieve legal fact download metadata for sendNotificationId {} and legalFactId {} from SEND", sendNotificationId, legalFactId);
    LegalFactDownloadMetadataDTO response = sendFacadeService.retrieveLegalFactDownloadMetadata(sendNotificationId, legalFactId, SecurityUtils.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> downloadAndCacheSendLegalFact(String notificationRequestId, LegalFactCategoryDTO category, String fileName) {
    log.info("Upload legal-fact having category {} for notificationRequestId {}", category, notificationRequestId);
    try {
      sendFacadeService.downloadAndCacheSendLegalFact(notificationRequestId, category, fileName, SecurityUtils.getAccessToken());
    } catch (IOException e) {
      throw new HttpServerErrorException(
        HttpStatusCode.valueOf(500),
        e.getMessage()
      );
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
