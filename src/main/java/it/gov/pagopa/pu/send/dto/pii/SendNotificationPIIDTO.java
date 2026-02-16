package it.gov.pagopa.pu.send.dto.pii;

import java.util.List;

import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import it.gov.pagopa.pu.send.dto.PuRecipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationPIIDTO implements PIIDTO {
  private List<PuRecipient> puRecipients;
}
