package it.gov.pagopa.pu.send.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class PuRecipientNoPIIDTO implements Serializable {
  private byte[] fiscalCodeHash;
  private List<PuPayment> puPayments;
}
