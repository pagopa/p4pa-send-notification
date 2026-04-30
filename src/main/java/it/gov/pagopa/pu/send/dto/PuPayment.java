package it.gov.pagopa.pu.send.dto;

import it.gov.pagopa.pu.send.dto.generated.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@FieldNameConstants
public class PuPayment implements Serializable {
  private Long debtPositionId;
  private Payment payment;
  private OffsetDateTime notificationDate;
}
