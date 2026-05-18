package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.enums.PaymentAvailable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("send_taxonomy")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
public class SendTaxonomy extends BaseEntity {
  @Id
  private String sendTaxonomyId;
  @NotNull
  private String organizationType;
  @NotNull
  private String organizationTypeDescription;
  @NotNull
  private String macroAreaCode;
  @NotNull
  private String macroAreaDescription;
  @NotNull
  private String serviceTypeCode;
  @NotNull
  private String serviceTypeDescription;
  private PaymentAvailable paymentAvailable;
  @NotNull
  private String taxonomyCode;
}
