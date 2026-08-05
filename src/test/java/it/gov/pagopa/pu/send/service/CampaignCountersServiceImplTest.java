package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationStatusV26DTO;
import it.gov.pagopa.pu.send.connector.send.generated.dto.TimelineElementCategoryV27DTO;
import it.gov.pagopa.pu.send.dto.Counters;
import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CampaignCountersServiceImplTest {
  @InjectMocks
  private CampaignCountersServiceImpl campaignCountersService;

  @Test
  void givenNullHistoryWhenCalculateActiveCountersThenReturnEmptySet() {
    Set<String> result = campaignCountersService.calculateActiveCounters(null);

    assertEquals(Collections.emptySet(), result);
  }

  @Test
  void givenEmptyHistoryWhenCalculateActiveCountersThenReturnEmptySet() {
    Set<String> result = campaignCountersService.calculateActiveCounters(List.of());

    assertEquals(Collections.emptySet(), result);
  }

  @Test
  void givenHistoryWithWildcardMatchWhenCalculateActiveCountersThenReturnActiveCounter() {
    StreamEventSummaryDTO event = new StreamEventSummaryDTO(
      NotificationStatusV26DTO.ACCEPTED,
      TimelineElementCategoryV27DTO.SEND_SIMPLE_REGISTERED_LETTER
    );

    Set<String> result = campaignCountersService.calculateActiveCounters(List.of(event));

    assertEquals(Set.of(Counters.Fields.accepted), result);
  }

  @Test
  void givenHistoryWithMultipleIndependentEventsWhenCalculateActiveCountersThenReturnAllActive() {
    StreamEventSummaryDTO event1 = new StreamEventSummaryDTO(
      NotificationStatusV26DTO.ACCEPTED,
      null
    );
    StreamEventSummaryDTO event2 = new StreamEventSummaryDTO(
      NotificationStatusV26DTO.DELIVERED,
      null
    );

    Set<String> result = campaignCountersService.calculateActiveCounters(List.of(event1, event2));

    assertEquals(Set.of(Counters.Fields.accepted, Counters.Fields.delivered), result);
    assertEquals(2, result.size());
    assertTrue(result.contains(Counters.Fields.accepted));
    assertTrue(result.contains(Counters.Fields.delivered));
  }

  @Test
  void givenHistoryWithConflictWhenCalculateActiveCountersThenExcludeDeactivatedCounters() {
    StreamEventSummaryDTO event1 = new StreamEventSummaryDTO(
      NotificationStatusV26DTO.EFFECTIVE_DATE,
      null
    );
    StreamEventSummaryDTO event2 = new StreamEventSummaryDTO(
      NotificationStatusV26DTO.DELIVERING,
      TimelineElementCategoryV27DTO.SEND_ANALOG_PROGRESS
    );

    Set<String> result = campaignCountersService.calculateActiveCounters(List.of(event1, event2));

    assertEquals(Set.of(Counters.Fields.completed), result);
  }
}
