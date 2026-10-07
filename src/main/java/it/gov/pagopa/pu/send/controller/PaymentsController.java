package it.gov.pagopa.pu.send.controller;

import it.gov.pagopa.pu.send.controller.generated.PaymentsApi;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.service.PaymentsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PaymentsController implements PaymentsApi {

  private final PaymentsService paymentsService;

  public PaymentsController(PaymentsService paymentsService) {
    this.paymentsService = paymentsService;
  }

  @Override
  public ResponseEntity<SendNotificationDTO> notifyPayment(Long organizationId, String nav) {
    log.info("Notify payment having organizationId {} and nav {}", organizationId, nav);
    return ResponseEntity.ok(paymentsService.notifyPayment(organizationId, nav));
  }
}
