package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;

public interface PaymentsService {

  SendNotificationDTO notifyPayment(Long organizationId, String nav);
}
