package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationFiltersDTO {
    private String campaignId;
    private Long organizationId;
    private String iun;
    private OffsetDateTime dateFrom;
    private OffsetDateTime dateTo;
    private List<NotificationStatus> statuses;
    private byte[] fiscalCodeHash;
}
