package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentsServiceImpl implements PaymentsService {

  private final SendNotificationNoPIIRepository sendNotificationNoPIIRepository;
  private final SendNotificationPaidService sendNotificationPaidService;
  private final SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper;

  public PaymentsServiceImpl(SendNotificationNoPIIRepository sendNotificationNoPIIRepository, SendNotificationPaidService sendNotificationPaidService, SendNotification2SendNotificationDTOMapper sendNotificationDTOMapper) {
    this.sendNotificationNoPIIRepository = sendNotificationNoPIIRepository;
    this.sendNotificationPaidService = sendNotificationPaidService;
    this.sendNotificationDTOMapper = sendNotificationDTOMapper;
  }

  @Override
  public SendNotificationDTO notifyPayment(Long organizationId, String nav) {
    SendNotificationNoPII notification = sendNotificationNoPIIRepository.updatePaymentStatusByOrganizationIdAndNav(organizationId, nav, InstallmentStatus.PAID)
      .orElseThrow(() -> new SendNotificationNotFoundException("Notification not found with orgId %s and nav %s".formatted(organizationId, nav)));

    notification = sendNotificationPaidService.handlePaidNotification(notification);

    return sendNotificationDTOMapper.mapToSendNotificationDTO(notification);
  }
}
