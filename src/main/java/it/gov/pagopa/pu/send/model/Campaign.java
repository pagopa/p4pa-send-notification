package it.gov.pagopa.pu.send.model;

import it.gov.pagopa.pu.send.dto.Counters;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document("send_campaign")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
public class Campaign extends BaseEntity {
  @Id
  private String campaignId;
  @NotEmpty
  private String externalId;
  private String campaignName;
  @NotNull
  private Long organizationId;
  private String orgSubUnitCode;
  @NotNull
  @Builder.Default
  private Counters counters = new Counters();
  @NotNull
  private LocalDate startDate;
  @NotNull
  private LocalDate endDate;
}
