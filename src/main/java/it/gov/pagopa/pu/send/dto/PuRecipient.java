package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.dto.generated.Recipient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class PuRecipient {
  private Recipient recipient;
  private List<PuPayment> puPayments;
}
