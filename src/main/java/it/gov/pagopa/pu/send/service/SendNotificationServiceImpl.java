package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.CreateNotificationRequest;
import it.gov.pagopa.pu.send.dto.generated.CreateNotificationResponse;
import it.gov.pagopa.pu.send.utils.Constants.NOTIFICATION_STATUS;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationServiceImpl implements SendNotificationService {

  @Override
  public CreateNotificationResponse createSendNotification(
    List<CreateNotificationRequest> newNotifications) {
    return CreateNotificationResponse.builder().sendNotificationId(1L).status(
      NOTIFICATION_STATUS.WAITING_FILE.name()).build();
  }
}
