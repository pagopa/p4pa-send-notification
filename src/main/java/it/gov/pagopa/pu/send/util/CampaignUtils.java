package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CampaignUtils {
  private CampaignUtils() {}

  public static final Map<TimelineElementCategoryV27DTO, Set<String>> ORDERED_TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS = Stream.of(
    Map.entry(TimelineElementCategoryV27DTO.REQUEST_ACCEPTED, Set.of(Counters.Fields.accepted)),
    Map.entry(TimelineElementCategoryV27DTO.DIGITAL_SUCCESS_WORKFLOW, Set.of(Counters.Fields.accepted, Counters.Fields.delivered)),
    Map.entry(TimelineElementCategoryV27DTO.ANALOG_SUCCESS_WORKFLOW, Set.of(Counters.Fields.accepted, Counters.Fields.delivered)),
    Map.entry(TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion)),
    Map.entry(TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.analogicCompleted)),
    Map.entry(TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion)),
    Map.entry(TimelineElementCategoryV27DTO.SEND_DIGITAL_FEEDBACK, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.digitalCompleted))
  ).collect(Collectors.toMap(
    Map.Entry::getKey,
    Map.Entry::getValue,
    (a, b) -> a,
    LinkedHashMap::new
  ));

  public static final Map<String, Set<TimelineElementCategoryV27DTO>> COUNTER_FIELD2TIMELINE_ELEMENT_CATEGORIES =
    Collections.unmodifiableMap(
      ORDERED_TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
          .map(counterName -> Map.entry(counterName, entry.getKey())))
        .collect(Collectors.groupingBy(
          Map.Entry::getKey,
          Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
        ))
    );


  private static final List<TimelineElementCategoryV27DTO> TIMELINE_CATEGORY_ORDER =
    ORDERED_TIMELINE_ELEMENT_CATEGORY2COUNTER_FIELDS.keySet()
      .stream()
      .toList();

  public static final Comparator<TimelineElementCategoryV27DTO> TIMELINE_CATEGORY_COMPARATOR =
    Comparator.comparingInt(CampaignUtils.TIMELINE_CATEGORY_ORDER::indexOf);
}
