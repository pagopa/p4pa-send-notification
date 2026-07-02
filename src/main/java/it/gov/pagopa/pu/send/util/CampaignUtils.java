package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CampaignUtils {
  private CampaignUtils() {}

  public static final Map<TimelineElementCategoryV27DTO, Set<String>> TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS = Map.of(
    TimelineElementCategoryV27DTO.REQUEST_ACCEPTED, Set.of(Counters.Fields.accepted),
    TimelineElementCategoryV27DTO.DIGITAL_SUCCESS_WORKFLOW, Set.of(Counters.Fields.accepted, Counters.Fields.delivered),
    TimelineElementCategoryV27DTO.ANALOG_SUCCESS_WORKFLOW, Set.of(Counters.Fields.accepted, Counters.Fields.delivered),
    TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion),
    TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.analogicCompleted),
    TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion),
    TimelineElementCategoryV27DTO.SEND_DIGITAL_FEEDBACK, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.digitalCompleted)
  );

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
}
