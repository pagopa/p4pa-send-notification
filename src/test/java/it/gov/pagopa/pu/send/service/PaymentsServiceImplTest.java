package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.send.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.send.exception.SendNotificationNotFoundException;
import it.gov.pagopa.pu.send.mapper.SendNotification2SendNotificationDTOMapper;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.pu.send.repository.SendNotificationNoPIIRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceImplTest {

  @Mock
  private SendNotificationNoPIIRepository sendNotificationNoPIIRepositoryMock;
  @Mock
  private SendNotificationPaidService sendNotificationPaidServiceMock;
  @Mock
  private SendNotification2SendNotificationDTOMapper sendNotificationDTOMapperMock;

  @InjectMocks
  private PaymentsServiceImpl paymentsService;

  @AfterEach
  void verifyNoInteractions() {
    Mockito.verifyNoMoreInteractions(
      sendNotificationNoPIIRepositoryMock,
      sendNotificationPaidServiceMock,
      sendNotificationDTOMapperMock
    );
  }

  @Test
  void givenExistentNotificationWhenNotifyPaymentThenReturnMappedNotification() {
    Long organizationId = 1L;
    String nav = "NAV";

    SendNotificationNoPII notification = new SendNotificationNoPII();
    SendNotificationNoPII handledNotification = new SendNotificationNoPII();
    SendNotificationDTO expectedResult = new SendNotificationDTO();

    when(sendNotificationNoPIIRepositoryMock.updatePaymentStatusByOrganizationIdAndNav(organizationId, nav, InstallmentStatus.PAID))
      .thenReturn(Optional.of(notification));

    when(sendNotificationPaidServiceMock.handlePaidNotification(notification))
      .thenReturn(handledNotification);

    when(sendNotificationDTOMapperMock.mapToSendNotificationDTO(handledNotification))
      .thenReturn(expectedResult);

    SendNotificationDTO result = paymentsService.notifyPayment(organizationId, nav);

    Assertions.assertSame(expectedResult, result);

    verify(sendNotificationNoPIIRepositoryMock)
      .updatePaymentStatusByOrganizationIdAndNav(organizationId, nav, InstallmentStatus.PAID);
    verify(sendNotificationPaidServiceMock)
      .handlePaidNotification(notification);
    verify(sendNotificationDTOMapperMock)
      .mapToSendNotificationDTO(handledNotification);
  }

  @Test
  void givenNotExistentNotificationWhenNotifyPaymentThenThrowNotFoundException() {
    Long organizationId = 1L;
    String nav = "NAV";

    when(sendNotificationNoPIIRepositoryMock.updatePaymentStatusByOrganizationIdAndNav(organizationId, nav, InstallmentStatus.PAID))
      .thenReturn(Optional.empty());

    SendNotificationNotFoundException exception =
      Assertions.assertThrows(SendNotificationNotFoundException.class,
        () -> paymentsService.notifyPayment(organizationId, nav)
      );

    Assertions.assertEquals("Notification not found with orgId 1 and nav NAV", exception.getMessage());

    verify(sendNotificationNoPIIRepositoryMock)
      .updatePaymentStatusByOrganizationIdAndNav(organizationId, nav, InstallmentStatus.PAID);
  }
}
