package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.send.dto.generated.NotificationStatusV26DTO;
import it.gov.pagopa.send.dto.generated.TimelineElementCategoryV27DTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationPaidServiceImplTest {

  @Mock
  private SendNotificationStreamEventService sendNotificationStreamEventServiceMock;

  @InjectMocks
  private SendNotificationPaidServiceImpl sendNotificationPaidService;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(sendNotificationStreamEventServiceMock);
  }

  @Test
  void givenNoPaymentsWhenHandlePaidNotificationThenReturnEmpty() {
    // Given
    SendNotificationNoPII notification = createNotification(List.of(), null);

    // When
    Optional<SendNotificationNoPII> result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void givenAllPaymentsPaidAndPuPaidHistoryAlreadyExistsWhenHandlePaidNotificationThenReturnEmpty() {
    // Given
    PuPayment paidPayment1 = PuPayment.builder()
      .status(InstallmentStatus.PAID)
      .build();

    PuPayment paidPayment2 = PuPayment.builder()
      .status(InstallmentStatus.PAID)
      .build();

    StreamEventSummaryDTO existingPuPaidEvent = new StreamEventSummaryDTO();

    existingPuPaidEvent.setNewNotificationStatus(NotificationStatusV26DTO.PU_PAID);
    existingPuPaidEvent.setTimelineElementCategory(TimelineElementCategoryV27DTO.PU_PAYMENT);

    SendNotificationNoPII notification = createNotification(List.of(paidPayment1, paidPayment2), new ArrayList<>(List.of(existingPuPaidEvent)));

    // When
    Optional<SendNotificationNoPII> result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void givenOnePaymentPaidAndNullHistoryWhenHandlePaidNotificationThenNotifyAndAppendPuPaidEvent() {
    // Given
    PuPayment paidPayment1 = PuPayment.builder()
      .status(InstallmentStatus.PAID)
      .build();

    PuPayment paidPayment2 = PuPayment.builder()
      .status(null)
      .build();

    SendNotificationNoPII notification = createNotification(List.of(paidPayment1, paidPayment2), null);

    doNothing()
      .when(sendNotificationStreamEventServiceMock)
      .notifySendNotificationStreamEvents(
        notification, List.of(new StreamEventSummaryDTO(NotificationStatusV26DTO.PU_PAID, TimelineElementCategoryV27DTO.PU_PAYMENT))
      );

    // When
    Optional<SendNotificationNoPII> result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertFalse(result.isEmpty());

    SendNotificationNoPII resultNotification = result.get();

    assertSame(notification, resultNotification);
    assertNotNull(resultNotification.getHistory());
    assertEquals(1, resultNotification.getHistory().size());

    StreamEventSummaryDTO addedEvent = resultNotification.getHistory().getFirst();

    assertEquals(NotificationStatusV26DTO.PU_PAID, addedEvent.getNewNotificationStatus());
    assertEquals(TimelineElementCategoryV27DTO.PU_PAYMENT, addedEvent.getTimelineElementCategory());
  }

  @Test
  void givenOnePaymentPaidAndHistoryWithoutPuPaidWhenHandlePaidNotificationThenAppendPuPaidEvent() {
    // Given
    PuPayment paidPayment1 = PuPayment.builder()
      .status(InstallmentStatus.PAID)
      .build();

    PuPayment paidPayment2 = PuPayment.builder()
      .status(null)
      .build();

    StreamEventSummaryDTO existingEvent = new StreamEventSummaryDTO();

    existingEvent.setNewNotificationStatus(NotificationStatusV26DTO.ACCEPTED);

    SendNotificationNoPII notification = createNotification(List.of(paidPayment1, paidPayment2), new ArrayList<>(List.of(existingEvent)));

    doNothing()
      .when(sendNotificationStreamEventServiceMock)
      .notifySendNotificationStreamEvents(
        notification, List.of(new StreamEventSummaryDTO(NotificationStatusV26DTO.PU_PAID, TimelineElementCategoryV27DTO.PU_PAYMENT))
      );

    // When
    Optional<SendNotificationNoPII> result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertFalse(result.isEmpty());

    SendNotificationNoPII resultNotification = result.get();

    assertSame(notification, resultNotification);
    assertEquals(2, resultNotification.getHistory().size());

    assertSame(existingEvent, resultNotification.getHistory().get(0));

    StreamEventSummaryDTO addedEvent = resultNotification.getHistory().get(1);

    assertEquals(NotificationStatusV26DTO.PU_PAID, addedEvent.getNewNotificationStatus());
    assertEquals(TimelineElementCategoryV27DTO.PU_PAYMENT, addedEvent.getTimelineElementCategory());
  }

  private SendNotificationNoPII createNotification(List<PuPayment> payments, List<StreamEventSummaryDTO> history) {
    PuRecipientNoPIIDTO recipient = new PuRecipientNoPIIDTO();
    recipient.setPuPayments(payments);

    SendNotificationNoPII notification = new SendNotificationNoPII();

    notification.setRecipients(List.of(recipient));
    notification.setHistory(history);

    return notification;
  }
}
