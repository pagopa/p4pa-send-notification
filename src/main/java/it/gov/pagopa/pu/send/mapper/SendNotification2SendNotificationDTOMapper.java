package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.model.SendNotification;
import org.springframework.stereotype.Service;

@Service
public class SendNotification2SendNotificationDTOMapper {

  public SendNotificationDTO apply(SendNotification sendNotification) {
    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    notificationDTO.setSendNotificationId(sendNotification.getSendNotificationId());
    notificationDTO.setOrganizationId(sendNotification.getOrganizationId());
    notificationDTO.setIun(sendNotification.getIun());
    notificationDTO.setNotificationDate(sendNotification.getNotificationData());
    notificationDTO.setStatus(sendNotification.getStatus().name());
    return notificationDTO;
  }

}
