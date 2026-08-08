package it.gov.pagopa.pu.send.service;

import it.gov.pagopa.pu.send.dto.generated.StreamEventSummaryDTO;

import java.util.List;
import java.util.Set;

public interface CampaignCountersService {
  Set<String> calculateActiveCounters(List<StreamEventSummaryDTO> history);

}
