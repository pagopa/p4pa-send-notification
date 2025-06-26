package it.gov.pagopa.pu.send.dto;

import java.util.List;

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
