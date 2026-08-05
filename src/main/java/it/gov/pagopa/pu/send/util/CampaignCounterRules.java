package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationStatusV26DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CampaignCounterRules {
  private CampaignCounterRules() {}

  // TODO: remove in P4ADEV-4923
  public static final Map<TimelineElementCategoryV27DTO, Set<String>> TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS = Map.of(
    TimelineElementCategoryV27DTO.REQUEST_ACCEPTED, Set.of(Counters.Fields.accepted),
    TimelineElementCategoryV27DTO.DIGITAL_SUCCESS_WORKFLOW, Set.of(Counters.Fields.accepted, Counters.Fields.delivered),
    TimelineElementCategoryV27DTO.ANALOG_SUCCESS_WORKFLOW, Set.of(Counters.Fields.accepted, Counters.Fields.delivered),
    TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion),
    TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.analogicCompleted),
    TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion),
    TimelineElementCategoryV27DTO.SEND_DIGITAL_FEEDBACK, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.digitalCompleted)
  );

  // TODO: remove in P4ADEV-4923
  public static final Map<String, Set<TimelineElementCategoryV27DTO>> COUNTER_FIELD2TIMELINE_ELEMENT_CATEGORIES =
    Collections.unmodifiableMap(
      TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
          .map(counterName -> Map.entry(counterName, entry.getKey())))
        .collect(Collectors.groupingBy(
          Map.Entry::getKey,
          Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
        ))
    );

  @Getter
  @Builder
  public static class CounterRule {
    @Builder.Default
    private List<StreamEventSummaryDTO> activationConditions = List.of();
    @Builder.Default
    private List<String> deactivatingCounters = List.of();
    @Builder.Default
    private List<StreamEventSummaryDTO> deactivationConditions = List.of();
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
        new StreamEventSummaryDTO(NotificationStatusV26DTO.VIEWED, null),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.EFFECTIVE_DATE, null)
      )).build(),
    Counters.Fields.analogicCompletionPreOutcome, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK)
      ))
      .deactivatingCounters(List.of(
        Counters.Fields.completed, Counters.Fields.analogicCompletion, Counters.Fields.deceasedRecipient, Counters.Fields.recipientNotFound
      )).build(),
    Counters.Fields.analogicCompletion, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.ANALOG_FAILURE_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.ANALOG_SUCCESS_WORKFLOW)
      ))
      .deactivatingCounters(List.of(
        Counters.Fields.completed, Counters.Fields.deceasedRecipient, Counters.Fields.recipientNotFound
      )).build(),
    Counters.Fields.digitalCompletionDigitalDomicile, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.DIGITAL_FAILURE_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_SIMPLE_REGISTERED_LETTER),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.DIGITAL_SUCCESS_WORKFLOW)
      ))
      .deactivatingCounters(List.of(
        Counters.Fields.completed, Counters.Fields.deceasedRecipient, Counters.Fields.recipientNotFound
      )).build(),
    Counters.Fields.digitalCompletionCourtesyMessage, CounterRule.builder()
      .activationConditions(List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.PROBABLE_SCHEDULING_ANALOG_DATE),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SCHEDULE_ANALOG_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_COURTESY_MESSAGE)
      ))
      .deactivatingCounters(List.of(
        Counters.Fields.completed, Counters.Fields.deceasedRecipient, Counters.Fields.recipientNotFound
      )).build(),
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
