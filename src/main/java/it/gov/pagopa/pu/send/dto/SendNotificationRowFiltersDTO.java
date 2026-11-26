package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.enums.NotificationStatus;
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
public class SendNotificationRowFiltersDTO {
  private Long organizationId;
  private List<String> orgSubUnitCodes;
  private Boolean fetchAll;
  private String iun;
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private List<NotificationStatus> statuses;
  private byte[] fiscalCodeHash;
}
