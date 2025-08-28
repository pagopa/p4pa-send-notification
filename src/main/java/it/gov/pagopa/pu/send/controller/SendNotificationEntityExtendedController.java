package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.SendNotificationEntityExtendedControllerApi;
import it.gov.pagopa.pu.send.enums.NotificationStatus;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepositoryExtImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Controller to host spring-data-rest directly not supported methods */
@RestController
public class SendNotificationEntityExtendedController implements SendNotificationEntityExtendedControllerApi {

  private final SendNotificationNoPIIRepositoryExtImpl repository;

  public SendNotificationEntityExtendedController(SendNotificationNoPIIRepositoryExtImpl sendNotificationNoPIIRepositoryExt) {
    this.repository = sendNotificationNoPIIRepositoryExt;
  }

  @Override
  public ResponseEntity<Void> updateNotificationStatus(Long sendNotificationId, String status) {
    repository.updateNotificationStatus(String.valueOf(sendNotificationId), NotificationStatus.valueOf(status));
    return ResponseEntity.ok().build();
  }
}
