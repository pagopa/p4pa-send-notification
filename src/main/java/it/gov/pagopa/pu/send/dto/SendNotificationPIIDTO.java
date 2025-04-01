package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.dto.generated.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationPIIDTO implements PIIDTO {
  private String fiscalCode;
  private Address address;
}
