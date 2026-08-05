package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationStatusV26DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CampaignUtils {
  private CampaignUtils() {}

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

  public record CounterRule(
    List<StreamEventSummaryDTO> activationConditions,
    List<String> deactivatingCounters,
    List<StreamEventSummaryDTO> deactivationConditions
  ) {}

  public static final Map<String, CounterRule> COUNTER_RULES = Map.of(
    Counters.Fields.accepted, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.ACCEPTED, null)
      ),
      List.of(),
      List.of()
    ),
    Counters.Fields.delivered, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, null)
      ),
      List.of(),
      List.of()
    ),
    Counters.Fields.completed, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.VIEWED, null),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.EFFECTIVE_DATE, null)
      ),
      List.of(),
      List.of()
    ),
    Counters.Fields.analogicCompletionPreOutcome, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK)
      ),
      List.of(Counters.Fields.completed, Counters.Fields.analogicCompletion, Counters.Fields.deceasedRecipient, Counters.Fields.recipientNotFound),
      List.of()
    ),
    Counters.Fields.analogicCompletion, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.ANALOG_FAILURE_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.ANALOG_SUCCESS_WORKFLOW)
      ),
      List.of(Counters.Fields.completed, Counters.Fields.deceasedRecipient, Counters.Fields.recipientNotFound),
      List.of()
    ),
    Counters.Fields.digitalCompletionDigitalDomicile, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.DIGITAL_FAILURE_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_SIMPLE_REGISTERED_LETTER),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERED, TimelineElementCategoryV27DTO.DIGITAL_SUCCESS_WORKFLOW)
      ),
      List.of(Counters.Fields.completed, Counters.Fields.deceasedRecipient, Counters.Fields.recipientNotFound),
      List.of()
    ),
    Counters.Fields.digitalCompletionCourtesyMessage, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.PROBABLE_SCHEDULING_ANALOG_DATE),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SCHEDULE_ANALOG_WORKFLOW),
        new StreamEventSummaryDTO(NotificationStatusV26DTO.DELIVERING, TimelineElementCategoryV27DTO.SEND_COURTESY_MESSAGE)
      ),
      List.of(Counters.Fields.completed, Counters.Fields.deceasedRecipient, Counters.Fields.recipientNotFound),
      List.of()
    ),
    Counters.Fields.failed, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.REFUSED, null)
      ),
      List.of(),
      List.of()
    ),
    Counters.Fields.deceasedRecipient, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.RETURNED_TO_SENDER, null)
      ),
      List.of(),
      List.of()
    ),
    Counters.Fields.recipientNotFound, new CounterRule(
      List.of(
        new StreamEventSummaryDTO(NotificationStatusV26DTO.UNREACHABLE, null)
      ),
      List.of(Counters.Fields.completed),
      List.of()
    )
  );
}
