package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;
import it.gov.pagopa.pu.send.util.CampaignCounterRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static it.gov.pagopa.pu.send.util.CampaignCounterRules.COUNTER_RULES;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignCountersServiceImpl implements CampaignCountersService {
  @Override
  public Set<String> calculateActiveCounters(List<StreamEventSummaryDTO> history) {
    if (history == null || history.isEmpty()) {
      return Collections.emptySet();
    }

    log.trace("Evaluating candidate counters from COUNTER_RULES based on history events: {}", history);

    Set<String> candidateCounters = COUNTER_RULES.entrySet().stream()
      .filter(entry -> isCounterEligibleForActivation(entry.getKey(), entry.getValue(), history))
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());

    log.trace("Found {} eligible candidate counters before deactivation check: {}", candidateCounters.size(), candidateCounters);

    Set<String> finalCounters =candidateCounters.stream()
      .filter(candidate -> {
        CampaignCounterRules.CounterRule rule = COUNTER_RULES.get(candidate);
        return rule.getDeactivatingCounters().stream().noneMatch(candidateCounters::contains);
      })
      .collect(Collectors.toSet());

    log.trace("Final active counters calculated: {}", finalCounters);

    return finalCounters;
  }

  private boolean isCounterEligibleForActivation(String counterName, CampaignCounterRules.CounterRule rule, List<StreamEventSummaryDTO> history) {
    boolean matchesActivation = rule.getActivationConditions().stream()
      .anyMatch(condition -> hasMatchingEventInHistory(condition, history));

    if (!matchesActivation) {
      log.trace("Counter '{}' discarded: no matching activation conditions found.", counterName);
      return false;
    }

    boolean matchesDeactivation = rule.getDeactivationConditions().stream()
      .anyMatch(condition -> hasMatchingEventInHistory(condition, history));

    if (matchesDeactivation) {
      log.trace("Counter '{}' discarded: matching deactivation conditions found.", counterName);
    } else {
      log.trace("Counter '{}' eligible: activation conditions met and no deactivation conditions found.", counterName);
    }

    return !matchesDeactivation;
  }

  private boolean hasMatchingEventInHistory(StreamEventSummaryDTO condition, List<StreamEventSummaryDTO> history) {
    return history.stream().anyMatch(event ->
      Objects.equals(condition.getNewNotificationStatus(), event.getNewNotificationStatus()) &&
        (condition.getTimelineElementCategory() == null ||
          Objects.equals(condition.getTimelineElementCategory(), event.getTimelineElementCategory()))
    );
  }
}
