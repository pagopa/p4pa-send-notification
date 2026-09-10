package it.gov.pagopa.pu.send.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class CampaignCountersAggregationResult implements Serializable {
  private Counters counters;
  private LocalDate startDate;
  private LocalDate endDate;
}
