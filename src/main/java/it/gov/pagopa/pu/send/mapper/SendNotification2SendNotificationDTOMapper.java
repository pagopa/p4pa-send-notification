package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.PagoPa;
import it.gov.pagopa.pu.send.dto.generated.Payment;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationPaymentsDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SendNotification2SendNotificationDTOMapper {

  public SendNotificationDTO apply(SendNotificationNoPII sendNotificationNoPII) {
    SendNotificationDTO notificationDTO = new SendNotificationDTO();
    notificationDTO.setSendNotificationId(sendNotificationNoPII.getSendNotificationId());
    notificationDTO.setOrganizationId(sendNotificationNoPII.getOrganizationId());
    notificationDTO.setIun(sendNotificationNoPII.getIun());
    notificationDTO.setStatus(sendNotificationNoPII.getStatus());
    notificationDTO.setPayments(buildPayments(sendNotificationNoPII));
    return notificationDTO;
  }

  private static List<SendNotificationPaymentsDTO> buildPayments(SendNotificationNoPII sendNotificationNoPII) {
    return sendNotificationNoPII.getRecipients().stream()
      .flatMap(recipient -> recipient.getPuPayments().stream())
      .filter(p -> p != null && p.getDebtPositionId() != null)
      .collect(Collectors.groupingBy(PuPayment::getDebtPositionId))
      .entrySet().stream()
      .map(entry -> new SendNotificationPaymentsDTO(
        entry.getKey(),
        entry.getValue().stream()
          .map(p -> Optional.ofNullable(p.getPayment())
            .map(Payment::getPagoPa)
            .map(PagoPa::getNoticeCode)
            .orElse(null))
          .filter(Objects::nonNull)
          .toList(),
        entry.getValue().stream()
          .map(PuPayment::getNotificationDate)
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(null)
      ))
      .toList();
  }
}
