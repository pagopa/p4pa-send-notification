package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.SendApi;
import it.gov.pagopa.pu.send.service.SendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SendController implements SendApi {

  private final SendService sendService;

  public SendController(SendService sendService) {
    this.sendService = sendService;
  }

  @Override
  public ResponseEntity<Void> preloadSendFile(String sendNotificationId) {
    log.info("request preload files for sendNotificationId:{}", sendNotificationId);
    sendService.preloadFiles(sendNotificationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
