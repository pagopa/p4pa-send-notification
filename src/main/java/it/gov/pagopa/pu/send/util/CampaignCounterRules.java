package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.send.dto.generated.NotificationStatusV26DTO;
import it.gov.pagopa.send.dto.generated.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import lombok.*;

import java.util.*;
import java.util.stream.Stream;

public class CampaignCounterRules {
  private CampaignCounterRules() {}

  @Getter
  @Builder
  public static class CounterRule {
    @Builder.Default
    private List<StreamEventSummaryDTO> activationConditions = List.of();
    @Builder.Default
    private List<String> deactivatingCounters = List.of();
    @Builder.Default
    private List<StreamEventSummaryDTO> deactivationConditions = List.of(); // To be configured if deactivation logic is needed and a deactivation counter is missing, otherwise configuring the counter itself is preferred
  }

  private static final List<String> TERMINAL_COUNTERS = List.of(
    Counters.Fields.completed,
    Counters.Fields.deceasedRecipient,
    Counters.Fields.recipientNotFound
  );

  private static List<String> withTerminalCounters(String... additionalCounters) {
    return Stream.concat(
      TERMINAL_COUNTERS.stream(),
      Arrays.stream(additionalCounters)
    ).toList();
  }

  public static final Map<String, CounterRule> COUNTER_RULES = Map.of(
    Counters.Fields.accepted, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, null)
      )).build(),

    Counters.Fields.delivered, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, null)
      )).build(),

    Counters.Fields.completed, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.EFFECTIVE_DATE, null)
      )).build(),

    Counters.Fields.analogicCompletionPreOutcome, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK)
      ))
      .deactivatingCounters(withTerminalCounters(Counters.Fields.analogicCompletion)).build(),

    Counters.Fields.analogicCompletion, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.ANALOG_FAILURE_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.ANALOG_SUCCESS_WORKFLOW)
      ))
      .deactivatingCounters(TERMINAL_COUNTERS).build(),

    Counters.Fields.digitalCompletionDigitalDomicile, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.DIGITAL_FAILURE_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_SIMPLE_REGISTERED_LETTER),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.DIGITAL_SUCCESS_WORKFLOW)
      ))
      .deactivatingCounters(TERMINAL_COUNTERS).build(),

    Counters.Fields.digitalCompletionCourtesyMessage, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.PROBABLE_SCHEDULING_ANALOG_DATE),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SCHEDULE_ANALOG_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_COURTESY_MESSAGE),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.VIEWED, TimelineElementCategoryV27DTO.NOTIFICATION_VIEWED)
        ))
      // if it's not completed via courtesy message, the notification moves to a digital or analog domicile
      .deactivatingCounters(withTerminalCounters(Counters.Fields.digitalCompletionDigitalDomicile, Counters.Fields.analogicCompletion)).build(),

    Counters.Fields.failed, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.REFUSED, null)
      )).build(),

    Counters.Fields.deceasedRecipient, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.RETURNED_TO_SENDER, null)
      )).build(),

    Counters.Fields.recipientNotFound, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.UNREACHABLE, null)
      ))
      .deactivatingCounters(List.of(Counters.Fields.completed))
      .build()
  );
}
