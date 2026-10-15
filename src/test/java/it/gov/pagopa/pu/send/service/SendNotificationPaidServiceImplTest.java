package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.PuRecipientNoPIIDTO;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.send.dto.generated.NotificationStatusV26DTO;
import it.gov.pagopa.send.dto.generated.TimelineElementCategoryV27DTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SendNotificationPaidServiceImplTest {

  @Mock
  private SendNotificationStreamEventService sendNotificationStreamEventServiceMock;

  @InjectMocks
  private SendNotificationPaidServiceImpl sendNotificationPaidService;

  @Test
  void givenNotAllPaymentsPaidWhenHandlePaidNotificationThenDoNothing() {
    // Given
    PuPayment paidPayment = PuPayment.builder()
      .status(InstallmentStatus.PAID)
      .build();

    PuPayment notPaidPayment = PuPayment.builder()
      .status(null)
      .build();

    SendNotificationNoPII notification = createNotification(List.of(paidPayment, notPaidPayment), null);

    // When
    SendNotificationNoPII result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertSame(notification, result);
    assertNull(result.getHistory());

    verify(sendNotificationStreamEventServiceMock, never())
      .notifySendNotificationStreamEvents(Mockito.any(), Mockito.anyList());
  }

  @Test
  void givenNoPaymentsWhenHandlePaidNotificationThenDoNothing() {
    // Given
    SendNotificationNoPII notification = createNotification(List.of(), null);

    // When
    SendNotificationNoPII result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertSame(notification, result);

    verify(sendNotificationStreamEventServiceMock, never())
      .notifySendNotificationStreamEvents(Mockito.any(), Mockito.anyList());
  }

  @Test
  void givenAllPaymentsPaidAndPuPaidHistoryAlreadyExistsWhenHandlePaidNotificationThenDoNothing() {
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
    SendNotificationNoPII result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertSame(notification, result);
    assertEquals(1, result.getHistory().size());
    assertSame(existingPuPaidEvent, result.getHistory().getFirst());

    verify(sendNotificationStreamEventServiceMock, never())
      .notifySendNotificationStreamEvents(Mockito.any(), Mockito.anyList());
  }

  @Test
  void givenAllPaymentsPaidAndNullHistoryWhenHandlePaidNotificationThenNotifyAndAppendPuPaidEvent() {
    // Given
    PuPayment paidPayment1 = PuPayment.builder()
      .status(InstallmentStatus.PAID)
      .build();

    PuPayment paidPayment2 = PuPayment.builder()
      .status(InstallmentStatus.PAID)
      .build();

    SendNotificationNoPII notification = createNotification(List.of(paidPayment1, paidPayment2), null);

    // When
    SendNotificationNoPII result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertSame(notification, result);

    assertNotNull(result.getHistory());
    assertEquals(1, result.getHistory().size());

    StreamEventSummaryDTO addedEvent = result.getHistory().getFirst();

    assertEquals(NotificationStatusV26DTO.PU_PAID, addedEvent.getNewNotificationStatus());
    assertEquals(TimelineElementCategoryV27DTO.PU_PAYMENT, addedEvent.getTimelineElementCategory());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<StreamEventSummaryDTO>> eventsCaptor = ArgumentCaptor.forClass(List.class);

    verify(sendNotificationStreamEventServiceMock)
      .notifySendNotificationStreamEvents(Mockito.same(notification), eventsCaptor.capture());

    List<StreamEventSummaryDTO> notifiedEvents = eventsCaptor.getValue();

    assertEquals(1, notifiedEvents.size());
    assertSame(addedEvent, notifiedEvents.getFirst());
  }

  @Test
  void givenAllPaymentsPaidAndHistoryWithoutPuPaidWhenHandlePaidNotificationThenAppendEventAsLast() {
    // Given
    PuPayment paidPayment = PuPayment.builder()
      .status(InstallmentStatus.PAID)
      .build();

    StreamEventSummaryDTO existingEvent = new StreamEventSummaryDTO();

    existingEvent.setNewNotificationStatus(NotificationStatusV26DTO.ACCEPTED);

    SendNotificationNoPII notification = createNotification(List.of(paidPayment), new ArrayList<>(List.of(existingEvent)));

    // When
    SendNotificationNoPII result = sendNotificationPaidService.handlePaidNotification(notification);

    // Then
    assertSame(notification, result);
    assertEquals(2, result.getHistory().size());

    assertSame(existingEvent, result.getHistory().get(0));

    StreamEventSummaryDTO addedEvent = result.getHistory().get(1);

    assertEquals(NotificationStatusV26DTO.PU_PAID, addedEvent.getNewNotificationStatus());
    assertEquals(TimelineElementCategoryV27DTO.PU_PAYMENT, addedEvent.getTimelineElementCategory());

    verify(sendNotificationStreamEventServiceMock)
      .notifySendNotificationStreamEvents(
        Mockito.same(notification),
        Mockito.argThat(events ->
          events.size() == 1
            && NotificationStatusV26DTO.PU_PAID.equals(
            events.getFirst().getNewNotificationStatus()
          )
            && TimelineElementCategoryV27DTO.PU_PAYMENT.equals(
            events.getFirst().getTimelineElementCategory()
          )
        )
      );
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
