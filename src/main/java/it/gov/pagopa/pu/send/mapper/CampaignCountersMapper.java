package it.gov.pagopa.pu.send.mapper;

import it.gov.pagopa.pu.send.dto.CampaignCountersAggregationResult;
import it.gov.pagopa.pu.send.dto.Counters;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CampaignCountersMapper {
  Counters toCounters(CampaignCountersAggregationResult result);
}
