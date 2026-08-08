package it.gov.pagopa.pu.send.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignFiltersDTO {
  private Long organizationId;
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private List<String> orgSubUnitCodes;
  private String campaignName;
  private String externalCampaignId;
  private Boolean fetchAll;
}
