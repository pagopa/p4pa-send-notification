package it.gov.pagopa.pu.send.util;

import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CampaignUtils {
  private CampaignUtils() {}

  public static final Map<TimelineElementCategoryV27DTO, Set<String>> COUNTERS_STATUS_RELATION_MAP = Map.of(
    TimelineElementCategoryV27DTO.REQUEST_ACCEPTED, Set.of(Counters.Fields.accepted),
    TimelineElementCategoryV27DTO.DIGITAL_SUCCESS_WORKFLOW, Set.of(Counters.Fields.accepted, Counters.Fields.delivered),
    TimelineElementCategoryV27DTO.ANALOG_SUCCESS_WORKFLOW, Set.of(Counters.Fields.accepted, Counters.Fields.delivered),
    TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion),
    TimelineElementCategoryV27DTO.SEND_ANALOG_FEEDBACK, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion, Counters.Fields.analogicCompleted),
    TimelineElementCategoryV27DTO.SEND_DIGITAL_PROGRESS, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion),
    TimelineElementCategoryV27DTO.SEND_DIGITAL_FEEDBACK, Set.of(Counters.Fields.accepted, Counters.Fields.delivered, Counters.Fields.completion, Counters.Fields.digitalCompleted)
  );

  public static Set<TimelineElementCategoryV27DTO> getStreamEventStatusesForCounter(String counterName) {
    return COUNTERS_STATUS_RELATION_MAP.entrySet().stream()
      .filter(entry -> entry.getValue().contains(counterName))
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());
  }
}
