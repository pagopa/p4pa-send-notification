package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.dto.generated.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class PuPayment {
  private Long debtPositionId;
  private Payment payment;
}
