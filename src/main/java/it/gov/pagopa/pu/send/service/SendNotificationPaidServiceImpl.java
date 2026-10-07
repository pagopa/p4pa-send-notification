package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.send.dto.PuPayment;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.model.SendNotificationNoPII;
import it.gov.pagopa.send.dto.generated.NotificationStatusV26DTO;
import it.gov.pagopa.send.dto.generated.TimelineElementCategoryV27DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SendNotificationPaidServiceImpl implements SendNotificationPaidService {

  private final SendNotificationStreamEventService sendNotificationStreamEventService;

  private static final NotificationStatusV26DTO INTERNAL_IUN_PAID_STATUS = NotificationStatusV26DTO.PU_PAID;
  private static final TimelineElementCategoryV27DTO INTERNAL_IUN_PAID_CATEGORY = TimelineElementCategoryV27DTO.PU_PAYMENT;

  @Override
  public SendNotificationNoPII handlePaidNotification(SendNotificationNoPII notification) {
    if (!areAllPaymentsPaid(notification)) {
      return notification;
    }

    if (containsPuPaidHistory(notification)) {
      return notification;
    }

    StreamEventSummaryDTO puPaidEvent = buildPuPaidEvent();

    sendNotificationStreamEventService.notifySendNotificationStreamEvents(notification, List.of(puPaidEvent));

    appendHistory(notification, puPaidEvent);

    return notification;
  }

  private boolean areAllPaymentsPaid(SendNotificationNoPII notification) {
    List<PuPayment> payments = Optional
      .ofNullable(notification.getRecipients())
      .orElseGet(Collections::emptyList)
      .stream()
      .filter(Objects::nonNull)
      .flatMap(recipient ->
        Optional.ofNullable(recipient.getPuPayments())
          .orElseGet(Collections::emptyList)
          .stream()
      )
      .filter(Objects::nonNull)
      .toList();

    return !payments.isEmpty()
      && payments.stream().allMatch(payment ->
        InstallmentStatus.PAID.equals(payment.getStatus())
      );
  }

  private boolean containsPuPaidHistory(SendNotificationNoPII notification) {
    return Optional.ofNullable(notification.getHistory())
      .orElseGet(Collections::emptyList)
      .stream()
      .anyMatch(event ->
        INTERNAL_IUN_PAID_STATUS.equals(event.getNewNotificationStatus())
          && INTERNAL_IUN_PAID_CATEGORY.equals(event.getTimelineElementCategory())
      );
  }

  private StreamEventSummaryDTO buildPuPaidEvent() {
    StreamEventSummaryDTO event = new StreamEventSummaryDTO();

    event.setNewNotificationStatus(INTERNAL_IUN_PAID_STATUS);
    event.setTimelineElementCategory(INTERNAL_IUN_PAID_CATEGORY);

    return event;
  }

  private void appendHistory(SendNotificationNoPII notification, StreamEventSummaryDTO event) {
    List<StreamEventSummaryDTO> history =
      notification.getHistory() == null
        ? new ArrayList<>()
        : new ArrayList<>(notification.getHistory());

    history.add(event);
    notification.setHistory(history);
  }
}
