package it.gov.pagopa.pu.send.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@FieldNameConstants
public class CampaignCountersAggregationResult extends Counters implements Serializable {

  private LocalDate startDate;
  private LocalDate endDate;
}
