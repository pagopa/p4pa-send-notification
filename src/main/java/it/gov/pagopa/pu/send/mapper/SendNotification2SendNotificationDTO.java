package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.model.SendNotification;
import org.springframework.stereotype.Service;

@Service
public class SendNotification2SendNotificationDTO {

  public SendNotificationDTO map(SendNotification sendNotification) {
    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    notificationDTO.setSendNotificationId(sendNotification.getSendNotificationId());
    notificationDTO.setIun(sendNotification.getIun());
    notificationDTO.setNotificationDate(sendNotification.getNotificationData());
    notificationDTO.setStatus(sendNotification.getStatus().name());
    return notificationDTO;
  }

}
