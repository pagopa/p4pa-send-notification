package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationPaymentsDTO;
import it.gov.pagopa.pu.send.model.SendNotification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SendNotification2SendNotificationDTOMapper {

  public SendNotificationDTO apply(SendNotification sendNotification) {
    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    notificationDTO.setSendNotificationId(sendNotification.getSendNotificationId());
    notificationDTO.setOrganizationId(sendNotification.getOrganizationId());
    notificationDTO.setIun(sendNotification.getIun());
    notificationDTO.setNotificationDate(sendNotification.getNotificationData());
    notificationDTO.setStatus(sendNotification.getStatus().name());
    notificationDTO.setPayments(buildPayments(sendNotification));
    return notificationDTO;
  }

  private static List<SendNotificationPaymentsDTO> buildPayments(SendNotification sendNotification) {
    return sendNotification.getPayments().stream()
      .collect(Collectors.groupingBy(PuPayment::getDebtPositionId))
      .entrySet().stream()
      .map(e -> new SendNotificationPaymentsDTO(
          e.getKey(),
          e.getValue().stream().map(p -> p.getPayment().getPagoPa().getNoticeCode()).toList()
        )
      ).toList();
  }

}
